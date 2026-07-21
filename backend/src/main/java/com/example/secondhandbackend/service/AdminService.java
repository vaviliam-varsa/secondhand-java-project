package com.example.secondhandbackend.service;

import com.example.secondhandbackend.dto.AdminUserResponse;
import com.example.secondhandbackend.dto.PendingAdvertisementResponse;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.enums.AdStatus;
import com.example.secondhandbackend.enums.UserStatus;
import com.example.secondhandbackend.exception.ResourceNotFoundException;
import com.example.secondhandbackend.repository.AdvertisementRepository;
import com.example.secondhandbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public AdminService(AdvertisementRepository advertisementRepository, UserRepository userRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    public List<PendingAdvertisementResponse> getPendingAdvertisements() {
        List<Advertisement> ads = advertisementRepository.findByStatus(AdStatus.PENDING);

        return ads.stream()
                .map(ad -> new PendingAdvertisementResponse(
                        ad.getId(),
                        ad.getTitle(),
                        new PendingAdvertisementResponse.OwnerInfo(
                                ad.getOwner().getId(), ad.getOwner().getFullName())
                ))
                .toList();
    }

    public void approveAdvertisement(Long adId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        ad.setStatus(AdStatus.ACTIVE);
        advertisementRepository.save(ad);
    }

    public void rejectAdvertisement(Long adId, String reason) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        ad.setStatus(AdStatus.REJECTED);
        advertisementRepository.save(ad);
    }

    public List<AdminUserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(u -> new AdminUserResponse(u.getId(), u.getFullName(), u.getUsername(), u.getStatus().name()))
                .toList();
    }

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
    }

    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}