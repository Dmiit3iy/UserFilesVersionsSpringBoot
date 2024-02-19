package org.dmiit3iy.service;

import org.dmiit3iy.dto.ResponseResult;
import org.dmiit3iy.model.User;
import org.dmiit3iy.model.UserFile;
import org.dmiit3iy.repository.UserFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
            //TODO Добавить в юзерфайл новое поле вершн
            //TODO при добавлении пользователем с  таким же именем как он ранее отправлял производить увелечение версии (ver1, ver2..)
          //  при возвращении списка файлов, возвращать с версиями,
            File fileRoot = new File("C:\\files");
            if (!fileRoot.exists()) {
                fileRoot.mkdirs();
            }

            User user = userService.get(Long.valueOf(id));
            String name = document.getOriginalFilename();

            UserFile userFile = new UserFile();
            userFile.setFilename(name);
            UserFile userFileNew = userFileRepository.save(userFile);
            userFileNew.setUser(user);

            UserFile userFile1 = userFileRepository.save(userFileNew);
            String serverFilename = userFile1.getId() + "." + name.substring(name.indexOf(".") + 1);
            userFile1.setServerFilename(serverFilename);


            byte[] bytes = document.getBytes();
            try (BufferedOutputStream bufferedOutputStream
                         = new BufferedOutputStream(new FileOutputStream(new File(fileRoot, serverFilename)))) {
                bufferedOutputStream.write(bytes);
            }
            return userFileRepository.save(userFile1);
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
    public UserFile get(long id, String filename, String ver) {
        return userFileRepository.findUserFileByFilenameAndUserId(filename, id);

    }

    @Override
    public UserFile update(UserFile userFile) {
        long id = userFile.getUser().getId();
        String fileName = userFile.getFilename();
        UserFile base = userFileRepository.findUserFileByFilenameAndUserId(fileName, id);

        base.setServerFilename(userFile.getServerFilename());

        return userFileRepository.save(base);
    }
}
