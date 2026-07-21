package com.example.secondhandbackend.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Long id;
    private String content;
    private LocalDateTime sentAt;
    private Long senderId;

    public ChatMessageResponse(Long id, String content, LocalDateTime sentAt, Long senderId) {
        this.id = id;
        this.content = content;
        this.sentAt = sentAt;
        this.senderId = senderId;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public Long getSenderId() { return senderId; }
}