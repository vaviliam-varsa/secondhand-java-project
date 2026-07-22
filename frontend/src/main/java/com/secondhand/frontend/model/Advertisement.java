package com.secondhand.frontend.model;

public class Advertisement {
    public Long id;
    public String title;
    public Long price;
    public String city;
    public String category;
    public String status;
    public String createdAt;

    @Override
    public String toString() {
        String priceStr = price != null ? String.format("%,d", price) : "-";
        return title + "  |  " + priceStr + " تومان  |  " + city + "  |  " + category;
    }
}