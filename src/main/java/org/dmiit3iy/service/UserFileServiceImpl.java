package org.dmiit3iy.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.lingala.zip4j.ZipFile;
import org.dmiit3iy.dto.ResponseResult;
import org.dmiit3iy.model.User;
import org.dmiit3iy.model.UserDetailsImpl;
import org.dmiit3iy.model.UserFile;
import org.dmiit3iy.repository.UserFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserFileServiceImpl implements UserFileService {
    private UserService userService;
    private UserFileRepository userFileRepository;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @Autowired
    public void setUserFileRepository(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    @Override
    public UserFile add(Authentication authentication, MultipartFile document) {
        if (authentication != null && authentication.isAuthenticated()) {
            long idUser = ((UserDetailsImpl) authentication.getPrincipal()).getId();
            try {
                File fileRoot = new File("C:\\files");
                if (!fileRoot.exists()) {
                    fileRoot.mkdirs();
                }

                User user = userService.get(idUser);
                String name = document.getOriginalFilename();

                UserFile userFile = new UserFile();
                userFile.setFilename(name);
                userFile.setUser(user);
                if (!userFileRepository.findUserFilesByUserId(idUser).isEmpty()) {
                    int newVersion = userFileRepository.findUserFilesByUserId(idUser).stream().filter(x -> x.getFilename().equals(name)).mapToInt(x -> x.getVersion()).max().orElse(0);
                    userFile.setVersion(newVersion + 1);
                }
                UserFile userFileNew = userFileRepository.save(userFile);

                String serverFilename = userFileNew.getId() + "." + name.substring(name.indexOf(".") + 1);
                userFileNew.setServerFilename(serverFilename);
                byte[] bytes = document.getBytes();
                try (BufferedOutputStream bufferedOutputStream
                             = new BufferedOutputStream(new FileOutputStream(new File(fileRoot, serverFilename)))) {
                    bufferedOutputStream.write(bytes);
                }
                return userFileRepository.save(userFileNew);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("This file already added!");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    @Override
    public List<UserFile> get(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            long id = ((UserDetailsImpl) authentication.getPrincipal()).getId();
            return userFileRepository.findUserFilesByUserId(id);
        }
        return null;
    }

    @Override
    public UserFile get(Authentication authentication, String filename, int version) {
        if (authentication != null && authentication.isAuthenticated()) {
            long id = ((UserDetailsImpl) authentication.getPrincipal()).getId();
            return userFileRepository.findUserFileByFilenameAndUserIdAndVersion(filename, id, version);
        }
        return null;
    }


    /**
     * Возвращает zip архив со всеми загруженными файлами пользователя, проименованные в формате: имя_версия
     *
     * @param response
     * @param authentication
     * @throws IOException
     */
    @Override
    public void getZip(HttpServletResponse response, Authentication authentication) throws IOException {
        List<UserFile> userFileList = get(authentication);
        if (!userFileList.isEmpty()) {
            File fileRoot = new File("C:\\files");
            File tmp = new File(fileRoot, "tmp");
            tmp.mkdirs();
            for (UserFile x : userFileList) {
                File f = new File(fileRoot, x.getServerFilename());
                File fNew = new File(tmp.getAbsolutePath(),
                        x.getFilename().substring(0, x.getFilename().lastIndexOf(".")) + "_" + x.getVersion() +
                                x.getFilename().substring(x.getFilename().lastIndexOf(".")));
                if (f.exists()) {
                    Files.copy(f.toPath(), fNew.toPath());
                }
            }

            try (ZipFile zipFile = new ZipFile(fileRoot.getAbsolutePath() + "\\userFiles.zip")) {
                File[] files = tmp.listFiles();
                for (File f : files) {
                    zipFile.addFile(f);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            File zipSendFile = new File(fileRoot, "userFiles.zip");
            try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(zipSendFile))) {
                response.getOutputStream().write(stream.readAllBytes());
                String mime = Files.probeContentType(zipSendFile.toPath());
                response.setContentType(mime);
            } catch (Exception e) {
                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getWriter(),
                        new ResponseResult<>(null, "Error file uploading"));
            } finally {
                String[] entries = tmp.list();
                for (String s : entries) {
                    File currentFile = new File(tmp.getPath(), s);
                    currentFile.delete();
                }
                tmp.delete();
                zipSendFile.delete();
            }
        } else {
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(),
                    new ResponseResult<>(null, "The user does not have any files to download"));
        }

    }


    @Override
    public void getFileMime(HttpServletResponse response, Authentication authentication, String fileName, int version) throws IOException {

        UserFile userFile = this.get(authentication, fileName, version);
        String serverFileName = userFile.getServerFilename();
        File file = new File("C:\\files", serverFileName);
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            response.getOutputStream().write(stream.readAllBytes());
            String mime = Files.probeContentType(file.toPath());
            response.setContentType(mime);
        } catch (Exception e) {
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(),
                    new ResponseResult<>(null, "Error file uploading"));
        }
    }

    @Override
    public void getOneFileVersionsZip(HttpServletResponse response, Authentication authentication, String fileName) throws IOException {
        List<UserFile> userFileList = get(authentication).stream().
                filter(x -> x.getFilename().equals(fileName)).collect(Collectors.toList());
        if (!userFileList.isEmpty()) {
            File fileRoot = new File("C:\\files");
            File tmp = new File(fileRoot, "tmp");
            tmp.mkdirs();
            for (UserFile x : userFileList) {
                File f = new File(fileRoot, x.getServerFilename());
                File fNew = new File(tmp.getAbsolutePath(),
                        x.getFilename().substring(0, x.getFilename().lastIndexOf(".")) + "_" + x.getVersion() +
                                x.getFilename().substring(x.getFilename().lastIndexOf(".")));
                if (f.exists()) {
                    Files.copy(f.toPath(), fNew.toPath());
                }
            }

            try (ZipFile zipFile = new ZipFile(fileRoot.getAbsolutePath() + "\\userFiles.zip")) {
                File[] files = tmp.listFiles();
                for (File f : files) {
                    zipFile.addFile(f);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            File zipSendFile = new File(fileRoot, "userFiles.zip");
            try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(zipSendFile))) {
                response.getOutputStream().write(stream.readAllBytes());
                String mime = Files.probeContentType(zipSendFile.toPath());
                response.setContentType(mime);
            } catch (Exception e) {
                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getWriter(),
                        new ResponseResult<>(null, "Error file uploading"));
            } finally {
                String[] entries = tmp.list();
                for (String s : entries) {
                    File currentFile = new File(tmp.getPath(), s);
                    currentFile.delete();
                }
                tmp.delete();
                zipSendFile.delete();
            }
        } else {
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(),
                    new ResponseResult<>(null, "The user does not have any files to download"));
        }

    }


    public byte[] getFileByte(Authentication authentication, String fileName, int version) throws IOException {
        if (authentication != null && authentication.isAuthenticated()) {
            UserFile userFile = this.get(authentication, fileName, version);
            String serverFileName = userFile.getServerFilename();
            try (BufferedInputStream stream = new BufferedInputStream(
                    new FileInputStream(new File("C:\\files", serverFileName)))) {
                return stream.readAllBytes();
            }
        }
        return null;
    }

}
