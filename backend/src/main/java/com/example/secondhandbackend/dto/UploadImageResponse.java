package com.example.secondhandbackend.dto;

public class UploadImageResponse {

    private Long imageId;
    private String filePath;
    private String message;

    public UploadImageResponse(Long imageId, String filePath, String message) {
        this.imageId = imageId;
        this.filePath = filePath;
        this.message = message;
    }

    public Long getImageId() { return imageId; }
    public String getFilePath() { return filePath; }
    public String getMessage() { return message; }
}