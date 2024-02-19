package org.dmiit3iy.service;

import org.dmiit3iy.model.UserFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface UserFileService {
    UserFile add(String id, MultipartFile document);

    List<UserFile> get(long id);

    UserFile get(long id, String filename);

    UserFile update(UserFile userFile);
    void getFileMime(HttpServletResponse response, long id, String fileName) throws IOException;

    byte[] getFileByte(long id, String fileName) throws IOException;
}
