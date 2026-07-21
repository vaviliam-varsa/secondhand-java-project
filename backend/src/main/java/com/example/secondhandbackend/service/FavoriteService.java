package com.example.secondhandbackend.service;

import com.example.secondhandbackend.dto.FavoriteResponse;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.entity.Favorite;
import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.exception.AccessDeniedException;
import com.example.secondhandbackend.exception.DuplicateResourceException;
import com.example.secondhandbackend.exception.ResourceNotFoundException;
import com.example.secondhandbackend.repository.AdvertisementRepository;
import com.example.secondhandbackend.repository.FavoriteRepository;
import com.example.secondhandbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           AdvertisementRepository advertisementRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.advertisementRepository = advertisementRepository;
    }

    public List<FavoriteResponse> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);

        return favorites.stream()
                .map(fav -> new FavoriteResponse(
                        fav.getId(),
                        new FavoriteResponse.AdvertisementSummary(
                                fav.getAdvertisement().getId(),
                                fav.getAdvertisement().getTitle(),
                                fav.getAdvertisement().getPrice())
                ))
                .toList();
    }

    public void addFavorite(Long userId, Long advertisementId) {

        if (favoriteRepository.existsByUserIdAndAdvertisementId(userId, advertisementId)) {
            throw new DuplicateResourceException("Advertisement already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Advertisement ad = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setAdvertisement(ad);

        favoriteRepository.save(favorite);
    }

    public void removeFavorite(Long favoriteId, Long userId) {

        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));

        if (!favorite.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot remove this favorite");
        }

        favoriteRepository.delete(favorite);
    }
}