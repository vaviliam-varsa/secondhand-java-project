package com.example.secondhandbackend.dto;

public class CreateAdResponse {

    private Long id;
    private String message;

    public CreateAdResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}