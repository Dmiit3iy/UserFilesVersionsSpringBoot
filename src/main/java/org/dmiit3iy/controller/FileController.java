package org.dmiit3iy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dmiit3iy.dto.ResponseResult;
import org.dmiit3iy.model.UserFile;
import org.dmiit3iy.service.UserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
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
    public ResponseEntity<ResponseResult<String>> save(@RequestPart String id,
                                                       @RequestPart MultipartFile document) {
        try {
            userFileService.add(id, document);

            return new ResponseEntity<>(
                    new ResponseResult<>(null, "File uploaded successfully"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ResponseResult<>(null, "File already exist"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * возвращает файл для заданного id пользователя и оригинального имени файла
     *
     * @param id
     * @param fileName
     * @return
     * @throws IOException
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getFile(@PathVariable long id, @RequestParam String fileName) throws IOException {
        UserFile userFile = userFileService.get(id, fileName);
        String serverFileName = userFile.getServerFilename();
        try (BufferedInputStream stream = new BufferedInputStream(
                new FileInputStream(new File("C:\\files", serverFileName)))) {
            return stream.readAllBytes();
        }
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ResponseResult<List<UserFile>>> getList(@PathVariable long id) {
        List<UserFile> list = userFileService.get(id);
        return new ResponseEntity<>(new ResponseResult<>(null, list), HttpStatus.OK);
    }


    @GetMapping(value = "/mime/{fileName}")
    public void getFile(HttpServletResponse response, @PathVariable String fileName, @RequestParam long id) throws IOException {
        UserFile userFile = userFileService.get(id, fileName);
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

}
