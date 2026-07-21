package com.example.secondhandbackend.dto;

public class AdminUserResponse {

    private Long id;
    private String fullName;
    private String username;
    private String status;

    public AdminUserResponse(Long id, String fullName, String username, String status) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
}