package com.example.secondhandbackend.service;

import com.example.secondhandbackend.dto.AdvertisementListItemResponse;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.entity.Category;
import com.example.secondhandbackend.entity.City;
import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.enums.AdStatus;
import com.example.secondhandbackend.exception.ResourceNotFoundException;
import com.example.secondhandbackend.repository.AdvertisementRepository;
import com.example.secondhandbackend.repository.CategoryRepository;
import com.example.secondhandbackend.repository.CityRepository;
import com.example.secondhandbackend.repository.UserRepository;
import com.example.secondhandbackend.dto.AdvertisementDetailResponse;
import com.example.secondhandbackend.repository.AdImageRepository;
import com.example.secondhandbackend.exception.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final AdImageRepository adImageRepository;

    public AdvertisementService(AdvertisementRepository advertisementRepository,
                                UserRepository userRepository,
                                CategoryRepository categoryRepository,
                                CityRepository cityRepository,
                                AdImageRepository adImageRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.cityRepository = cityRepository;
        this.adImageRepository = adImageRepository;
    }

    public Advertisement createAdvertisement(Long ownerId, String title, String description,
                                             Long price, Long categoryId, Long cityId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        Advertisement ad = new Advertisement();
        ad.setTitle(title);
        ad.setDescription(description);
        ad.setPrice(price);
        ad.setOwner(owner);
        ad.setCategory(category);
        ad.setCity(city);
        ad.setStatus(AdStatus.PENDING);
        ad.setCreatedAt(LocalDateTime.now());

        return advertisementRepository.save(ad);
    }

    public List<AdvertisementListItemResponse> getActiveAdvertisements() {
        List<Advertisement> ads = advertisementRepository.findByStatus(AdStatus.ACTIVE);

        return ads.stream()
                .map(ad -> new AdvertisementListItemResponse(
                        ad.getId(),
                        ad.getTitle(),
                        ad.getPrice(),
                        ad.getCity().getName(),
                        ad.getCategory().getName(),
                        ad.getStatus().name(),
                        ad.getCreatedAt()
                ))
                .toList();
    }

    public AdvertisementDetailResponse getAdvertisementDetail(Long id) {

        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        List<String> imagePaths = adImageRepository.findByAdvertisementId(id)
                .stream()
                .map(image -> image.getFilePath())
                .toList();

        AdvertisementDetailResponse.OwnerInfo ownerInfo = new AdvertisementDetailResponse.OwnerInfo(
                ad.getOwner().getId(), ad.getOwner().getFullName());

        return new AdvertisementDetailResponse(
                ad.getId(),
                ad.getTitle(),
                ad.getDescription(),
                ad.getPrice(),
                ad.getCity().getName(),
                ad.getCategory().getName(),
                ad.getStatus().name(),
                ad.getCreatedAt(),
                imagePaths,
                ownerInfo
        );
    }

    public void updateAdvertisement(Long adId, Long userId, String title, String description, Long price) {

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        if (!ad.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this advertisement");
        }

        ad.setTitle(title);
        ad.setDescription(description);
        ad.setPrice(price);

        advertisementRepository.save(ad);
    }

    public void deleteAdvertisement(Long adId, Long userId) {

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        if (!ad.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this advertisement");
        }

        advertisementRepository.delete(ad);
    }

    public void markAsSold(Long adId, Long userId) {

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        if (!ad.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this advertisement");
        }

        ad.setStatus(AdStatus.SOLD);
        advertisementRepository.save(ad);
    }
}