package com.example.secondhandbackend.dto;

public class StartConversationResponse {

    private Long conversationId;
    private String message;

    public StartConversationResponse(Long conversationId, String message) {
        this.conversationId = conversationId;
        this.message = message;
    }

    public Long getConversationId() { return conversationId; }
    public String getMessage() { return message; }
}