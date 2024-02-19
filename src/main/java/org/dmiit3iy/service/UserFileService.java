package org.dmiit3iy.service;

import org.dmiit3iy.model.UserFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserFileService {
    UserFile add(String id, MultipartFile document);

    List<UserFile> get(long id);

    UserFile get(long id, String filename);

    UserFile update(UserFile userFile);
}
