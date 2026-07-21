package com.example.secondhandbackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdvertisementDetailResponse {

    private Long id;
    private String title;
    private String description;
    private Long price;
    private String city;
    private String category;
    private String status;
    private LocalDateTime createdAt;
    private List<String> images;
    private OwnerInfo owner;

    public AdvertisementDetailResponse(Long id, String title, String description, Long price,
                                       String city, String category, String status,
                                       LocalDateTime createdAt, List<String> images, OwnerInfo owner) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.city = city;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.images = images;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getPrice() { return price; }
    public String getCity() { return city; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getImages() { return images; }
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