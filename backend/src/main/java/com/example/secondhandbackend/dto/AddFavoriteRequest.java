package com.example.secondhandbackend.dto;

public class AddFavoriteRequest {

    private Long advertisementId;

    public Long getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(Long advertisementId) {
        this.advertisementId = advertisementId;
    }
}