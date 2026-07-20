package com.example.secondhandbackend.controller;

import com.example.secondhandbackend.dto.AdvertisementCreateRequest;
import com.example.secondhandbackend.dto.AdvertisementListItemResponse;
import com.example.secondhandbackend.dto.CreateAdResponse;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.service.AdvertisementService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping
    public List<AdvertisementListItemResponse> getAdvertisements() {
        return advertisementService.getActiveAdvertisements();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAdResponse createAdvertisement(@RequestBody AdvertisementCreateRequest request,
                                                Authentication authentication) {

        Long ownerId = (Long) authentication.getPrincipal();

        Advertisement ad = advertisementService.createAdvertisement(
                ownerId,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCategoryId(),
                request.getCityId()
        );

        return new CreateAdResponse(ad.getId(), "Advertisement submitted for review");
    }
}