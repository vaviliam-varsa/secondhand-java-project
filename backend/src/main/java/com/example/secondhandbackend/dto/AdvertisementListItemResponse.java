package com.example.secondhandbackend.dto;

import java.time.LocalDateTime;

public class AdvertisementListItemResponse {

    private Long id;
    private String title;
    private Long price;
    private String city;
    private String category;
    private String status;
    private LocalDateTime createdAt;

    public AdvertisementListItemResponse(Long id, String title, Long price, String city,
                                         String category, String status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.city = city;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Long getPrice() {
        return price;
    }

    public String getCity() {
        return city;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}