package com.example.secondhandbackend.controller;

import com.example.secondhandbackend.dto.AdvertisementCreateRequest;
import com.example.secondhandbackend.dto.AdvertisementListItemResponse;
import com.example.secondhandbackend.dto.CreateAdResponse;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.service.AdvertisementService;
import com.example.secondhandbackend.dto.AdvertisementDetailResponse;
import com.example.secondhandbackend.dto.AdvertisementUpdateRequest;
import com.example.secondhandbackend.dto.MessageResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/{id}")
    public AdvertisementDetailResponse getAdvertisementDetail(@PathVariable Long id) {
        return advertisementService.getAdvertisementDetail(id);
    }

    @PutMapping("/{id}")
    public MessageResponse updateAdvertisement(@PathVariable Long id,
                                               @RequestBody AdvertisementUpdateRequest request,
                                               Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        advertisementService.updateAdvertisement(
                id, userId, request.getTitle(), request.getDescription(), request.getPrice());

        return new MessageResponse("Advertisement updated successfully");
    }

    @DeleteMapping("/{id}")
    public MessageResponse deleteAdvertisement(@PathVariable Long id, Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        advertisementService.deleteAdvertisement(id, userId);

        return new MessageResponse("Advertisement deleted successfully");
    }

    @PutMapping("/{id}/sold")
    public MessageResponse markAsSold(@PathVariable Long id, Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        advertisementService.markAsSold(id, userId);

        return new MessageResponse("Advertisement marked as sold");
    }
}