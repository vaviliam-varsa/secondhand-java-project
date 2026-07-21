package com.example.secondhandbackend.controller;

import com.example.secondhandbackend.dto.AddFavoriteRequest;
import com.example.secondhandbackend.dto.FavoriteResponse;
import com.example.secondhandbackend.dto.MessageResponse;
import com.example.secondhandbackend.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<FavoriteResponse> getFavorites(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.getUserFavorites(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse addFavorite(@RequestBody AddFavoriteRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.addFavorite(userId, request.getAdvertisementId());
        return new MessageResponse("Added to favorites");
    }

    @DeleteMapping("/{id}")
    public MessageResponse removeFavorite(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.removeFavorite(id, userId);
        return new MessageResponse("Removed from favorites");
    }
}