package com.secondhand.frontend.model;

import java.util.List;

public class AdvertisementDetail {
    public Long id;
    public String title;
    public String description;
    public Long price;
    public String city;
    public String category;
    public String status;
    public String createdAt;
    public List<String> images;
    public Owner owner;
}