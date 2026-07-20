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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;

    public AdvertisementService(AdvertisementRepository advertisementRepository,
                                UserRepository userRepository,
                                CategoryRepository categoryRepository,
                                CityRepository cityRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.cityRepository = cityRepository;
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
}