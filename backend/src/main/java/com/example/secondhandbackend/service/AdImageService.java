package com.example.secondhandbackend.service;

import com.example.secondhandbackend.entity.AdImage;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.exception.AccessDeniedException;
import com.example.secondhandbackend.exception.InvalidInputException;
import com.example.secondhandbackend.exception.ResourceNotFoundException;
import com.example.secondhandbackend.repository.AdImageRepository;
import com.example.secondhandbackend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AdImageService {

    private final AdImageRepository adImageRepository;
    private final AdvertisementRepository advertisementRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public AdImageService(AdImageRepository adImageRepository,
                          AdvertisementRepository advertisementRepository) {
        this.adImageRepository = adImageRepository;
        this.advertisementRepository = advertisementRepository;
    }

    public AdImage uploadImage(Long advertisementId, Long userId, MultipartFile file) {

        Advertisement ad = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        if (!ad.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this advertisement");
        }

        if (file.isEmpty()) {
            throw new InvalidInputException("Uploaded file is empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String newFilename = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetPath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file", e);
        }

        AdImage image = new AdImage();
        image.setAdvertisement(ad);
        image.setFilePath(uploadDir + "/" + newFilename);

        return adImageRepository.save(image);
    }
}