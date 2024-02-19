package org.dmiit3iy.service;

import org.dmiit3iy.model.User;
import org.dmiit3iy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void add(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Пользователь уже добавлен!");
        }
    }

    @Override
    public User get(String login) {
        return userRepository.findByLogin(login).orElseThrow(() -> new IllegalArgumentException("Такого пользователя нет"));
    }

    @Override
    public User get(long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Такого пользователя нет"));
    }
}

