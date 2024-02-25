package org.dmiit3iy.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.dmiit3iy.dto.ResponseResult;
import org.dmiit3iy.model.User;
import org.dmiit3iy.model.UserFile;
import org.dmiit3iy.repository.UserFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

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
    public UserFile add(String id, MultipartFile document) {
        try {
            //TODO Добавить в юзерфайл новое поле вершн(+)
            //TODO при добавлении пользователем с  таким же именем как он ранее отправлял производить увелечение версии (ver1, ver2..)
            //  при возвращении списка файлов, возвращать с версиями,
            File fileRoot = new File("C:\\files");
            if (!fileRoot.exists()) {
                fileRoot.mkdirs();
            }
            long idUser= Long.valueOf(id);
            User user = userService.get(idUser);
            String name = document.getOriginalFilename();

            UserFile userFile = new UserFile();
            userFile.setFilename(name);
            userFile.setUser(user);
            if(!userFileRepository.findUserFilesByUserId(idUser).isEmpty()){
            int newVersion=userFileRepository.findUserFilesByUserId(idUser).stream().filter(x->x.getFilename().equals(name)).mapToInt(x->x.getVersion()).max().orElse(1);
            userFile.setVersion(newVersion+1);
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


    @Override
    public List<UserFile> get(long id) {
        return userFileRepository.findUserFilesByUserId(id);
    }

    //TODO !!!!!третий параметр
    @Override
    public UserFile get(long id, String filename, int version) {
        return userFileRepository.findUserFileByFilenameAndUserIdAndVersion(filename, id, version);
    }


    @Override
    public UserFile update(UserFile userFile) {
        long id = userFile.getUser().getId();
        String fileName = userFile.getFilename();
        int version = userFile.getVersion();
        UserFile base = userFileRepository.findUserFileByFilenameAndUserIdAndVersion(fileName, id, version);

        base.setServerFilename(userFile.getServerFilename());

        return userFileRepository.save(base);
    }


    public void getFileMime(HttpServletResponse response, long id, String fileName, int version) throws IOException {
        UserFile userFile = this.get(id, fileName, version);
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


    public byte[] getFileByte(long id, String fileName, int version) throws IOException {
        UserFile userFile = this.get(id, fileName, version);
        String serverFileName = userFile.getServerFilename();
        try (BufferedInputStream stream = new BufferedInputStream(
                new FileInputStream(new File("C:\\files", serverFileName)))) {
            return stream.readAllBytes();
        }
    }
}
