package org.dmiit3iy.service;

import org.dmiit3iy.model.UserFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface UserFileService {
    UserFile add(Authentication authentication, MultipartFile document);

    List<UserFile> get(Authentication authentication);

    UserFile get(Authentication authentication, String filename, int version);


    void getFileMime(HttpServletResponse response, Authentication authentication, String fileName, int version) throws IOException;

    byte[] getFileByte(Authentication authentication, String fileName, int version) throws IOException;
}
