package com.example.secondhandbackend.service;

import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.enums.Role;
import com.example.secondhandbackend.enums.UserStatus;
import com.example.secondhandbackend.exception.DuplicateResourceException;
import com.example.secondhandbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.secondhandbackend.config.JwtUtil;
import com.example.secondhandbackend.dto.LoginResponse;
import com.example.secondhandbackend.enums.UserStatus;
import com.example.secondhandbackend.exception.AuthenticationFailedException;
import com.example.secondhandbackend.exception.BlockedUserException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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

    public LoginResponse login(String username, String rawPassword) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BlockedUserException("User account is blocked");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }
}