package org.dmiit3iy.controller;

import org.dmiit3iy.dto.ResponseResult;
import org.dmiit3iy.model.User;
import org.dmiit3iy.model.UserDetailsImpl;
import org.dmiit3iy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")

public class UserController {
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    ResponseEntity<ResponseResult<User>> post(@RequestParam String fio, @RequestParam String login, @RequestParam String password) {
        try {
            User user = new User(fio, login, password);
            this.userService.add(user);
            return new ResponseEntity<>(new ResponseResult<>(null, user), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseResult<>(e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<ResponseResult<User>> getAuth(Authentication authentication) {
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userService.get(userDetails.getId());
            return new ResponseEntity<>(new ResponseResult<>(null, user), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ResponseResult<>("Ошибка аутентификации", null), HttpStatus.BAD_REQUEST);
    }
}
