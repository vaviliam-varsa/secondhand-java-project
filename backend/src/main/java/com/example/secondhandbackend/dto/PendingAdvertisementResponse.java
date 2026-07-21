package com.example.secondhandbackend.dto;

public class PendingAdvertisementResponse {

    private Long id;
    private String title;
    private OwnerInfo owner;

    public PendingAdvertisementResponse(Long id, String title, OwnerInfo owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public OwnerInfo getOwner() { return owner; }

    public static class OwnerInfo {
        private Long id;
        private String fullName;

        public OwnerInfo(Long id, String fullName) {
            this.id = id;
            this.fullName = fullName;
        }

        public Long getId() { return id; }
        public String getFullName() { return fullName; }
    }
}