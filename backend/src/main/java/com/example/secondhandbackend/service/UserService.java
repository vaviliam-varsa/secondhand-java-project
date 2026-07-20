package com.example.secondhandbackend.service;

import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.enums.Role;
import com.example.secondhandbackend.enums.UserStatus;
import com.example.secondhandbackend.exception.DuplicateResourceException;
import com.example.secondhandbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String fullName, String username, String rawPassword, String phoneNumber) {

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateResourceException("Phone number already exists");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhoneNumber(phoneNumber);
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }
}