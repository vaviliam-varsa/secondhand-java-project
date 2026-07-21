package com.example.secondhandbackend.dto;

import java.time.LocalDateTime;

public class ConversationListItemResponse {

    private Long id;
    private AdSummary advertisement;
    private UserSummary otherUser;
    private String lastMessage;
    private LocalDateTime lastMessageAt;

    public ConversationListItemResponse(Long id, AdSummary advertisement, UserSummary otherUser,
                                        String lastMessage, LocalDateTime lastMessageAt) {
        this.id = id;
        this.advertisement = advertisement;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
    }

    public Long getId() { return id; }
    public AdSummary getAdvertisement() { return advertisement; }
    public UserSummary getOtherUser() { return otherUser; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }

    public static class AdSummary {
        private Long id;
        private String title;

        public AdSummary(Long id, String title) {
            this.id = id;
            this.title = title;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
    }

    public static class UserSummary {
        private Long id;
        private String fullName;

        public UserSummary(Long id, String fullName) {
            this.id = id;
            this.fullName = fullName;
        }

        public Long getId() { return id; }
        public String getFullName() { return fullName; }
    }
}