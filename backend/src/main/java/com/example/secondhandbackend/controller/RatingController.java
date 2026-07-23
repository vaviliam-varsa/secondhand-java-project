package com.example.secondhandbackend.controller;

import com.example.secondhandbackend.dto.MessageResponse;
import com.example.secondhandbackend.dto.RatingCommentsPageResponse;
import com.example.secondhandbackend.dto.RatingSummaryResponse;
import com.example.secondhandbackend.dto.SubmitRatingRequest;
import com.example.secondhandbackend.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/api/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse submitRating(@RequestBody SubmitRatingRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ratingService.submitRating(userId, request.getAdvertisementId(), request.getScore(), request.getComment());
        return new MessageResponse("Rating submitted");
    }

    @GetMapping("/api/users/{id}/ratings")
    public RatingSummaryResponse getUserRatings(@PathVariable Long id) {
        return ratingService.getSellerRatings(id);
    }

    @GetMapping("/api/users/{id}/ratings/comments")
    public RatingCommentsPageResponse getUserRatingComments(
            @PathVariable Long id,
            @RequestParam(required = false) Integer limit) {
        return ratingService.getSellerComments(id, limit);
    }
}