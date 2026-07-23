package com.example.secondhandbackend.service;

import com.example.secondhandbackend.dto.AdminUserResponse;
import com.example.secondhandbackend.dto.PendingAdvertisementResponse;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.entity.Category;
import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.enums.AdStatus;
import com.example.secondhandbackend.enums.UserStatus;
import com.example.secondhandbackend.exception.DuplicateResourceException;
import com.example.secondhandbackend.exception.InvalidInputException;
import com.example.secondhandbackend.exception.ResourceNotFoundException;
import com.example.secondhandbackend.repository.AdvertisementRepository;
import com.example.secondhandbackend.repository.CategoryRepository;
import com.example.secondhandbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public AdminService(AdvertisementRepository advertisementRepository,
                        UserRepository userRepository,
                        CategoryRepository categoryRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
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
        ad.setRejectionReason(null);
        advertisementRepository.save(ad);
    }

    public void rejectAdvertisement(Long adId, String reason) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        ad.setStatus(AdStatus.REJECTED);
        ad.setRejectionReason(reason);
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

    // ---------- مدیریت دسته‌بندی ----------

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(String name) {
        String trimmed = validateName(name);

        if (categoryRepository.existsByNameIgnoreCase(trimmed)) {
            throw new DuplicateResourceException("دسته‌بندی با این نام قبلاً ثبت شده است");
        }

        Category category = new Category();
        category.setName(trimmed);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, String name) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String trimmed = validateName(name);

        boolean nameChanged = !trimmed.equalsIgnoreCase(category.getName());
        if (nameChanged && categoryRepository.existsByNameIgnoreCase(trimmed)) {
            throw new DuplicateResourceException("دسته‌بندی با این نام قبلاً ثبت شده است");
        }

        category.setName(trimmed);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (advertisementRepository.existsByCategoryIdAndStatusNot(id, AdStatus.DELETED)) {
            throw new InvalidInputException("این دسته‌بندی به یک یا چند آگهی متصل است و قابل حذف نیست");
        }

        categoryRepository.delete(category);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("نام دسته‌بندی نمی‌تواند خالی باشد");
        }
        return name.trim();
    }
}