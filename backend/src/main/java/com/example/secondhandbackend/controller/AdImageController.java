package com.example.secondhandbackend.controller;

import com.example.secondhandbackend.dto.UploadImageResponse;
import com.example.secondhandbackend.entity.AdImage;
import com.example.secondhandbackend.service.AdImageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/advertisements/{advertisementId}/images")
public class AdImageController {

    private final AdImageService adImageService;

    public AdImageController(AdImageService adImageService) {
        this.adImageService = adImageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadImageResponse uploadImage(@PathVariable Long advertisementId,
                                           @RequestParam("file") MultipartFile file,
                                           Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        AdImage image = adImageService.uploadImage(advertisementId, userId, file);

        return new UploadImageResponse(image.getId(), image.getFilePath(), "Image uploaded successfully");
    }
}