package com.example.secondhandbackend.controller;

import com.example.secondhandbackend.dto.MessageResponse;
import com.example.secondhandbackend.dto.RegisterRequest;
import com.example.secondhandbackend.service.UserService;
import com.example.secondhandbackend.dto.LoginRequest;
import com.example.secondhandbackend.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse register(@RequestBody RegisterRequest request) {
        userService.registerUser(
                request.getFullName(),
                request.getUsername(),
                request.getPassword(),
                request.getPhoneNumber()
        );
        return new MessageResponse("User registered successfully");
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request.getUsername(), request.getPassword());
    }
}