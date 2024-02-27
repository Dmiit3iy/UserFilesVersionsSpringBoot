package org.dmiit3iy.controller;

import org.dmiit3iy.dto.ResponseResult;
import org.dmiit3iy.model.UserDetailsImpl;
import org.dmiit3iy.model.UserFile;
import org.dmiit3iy.service.UserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    private UserFileService userFileService;

    @Autowired
    public void setUserFileService(UserFileService userFileService) {
        this.userFileService = userFileService;
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ResponseResult<String>> save(Authentication authentication, @RequestPart MultipartFile document) {
        try {
            userFileService.add(authentication, document);
            return new ResponseEntity<>(new ResponseResult<>(null, "File uploaded successfully"), HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseResult<>("File already exist", null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * возвращает файл для заданного id пользователя и оригинального имени файла
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    @GetMapping(path = "/byte/", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getFile(Authentication authentication, @RequestParam String fileName, int version) throws IOException {

        return userFileService.getFileByte(authentication, fileName, version);

    }

    @GetMapping()
    public ResponseEntity<ResponseResult<List<UserFile>>> getList(Authentication authentication) {
        List<UserFile> list = userFileService.get(authentication);
        return new ResponseEntity<>(new ResponseResult<>(null, list), HttpStatus.OK);

    }

    @GetMapping(value = "/mime/{fileName}")
    public void getFile(HttpServletResponse response, @PathVariable String fileName, Authentication authentication, @RequestParam int version) throws IOException {


        userFileService.getFileMime(response, authentication, fileName, version);
    }

    @GetMapping("/zip")
    public void getZip(HttpServletResponse response, Authentication authentication) throws IOException {
        userFileService.getZip(response, authentication);
    }

}
