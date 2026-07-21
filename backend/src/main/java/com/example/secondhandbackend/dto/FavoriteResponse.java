package com.example.secondhandbackend.dto;

public class FavoriteResponse {

    private Long id;
    private AdvertisementSummary advertisement;

    public FavoriteResponse(Long id, AdvertisementSummary advertisement) {
        this.id = id;
        this.advertisement = advertisement;
    }

    public Long getId() {
        return id;
    }

    public AdvertisementSummary getAdvertisement() {
        return advertisement;
    }

    public static class AdvertisementSummary {
        private Long id;
        private String title;
        private Long price;

        public AdvertisementSummary(Long id, String title, Long price) {
            this.id = id;
            this.title = title;
            this.price = price;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public Long getPrice() { return price; }
    }
}