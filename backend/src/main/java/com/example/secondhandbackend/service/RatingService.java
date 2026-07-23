package com.example.secondhandbackend.service;

import com.example.secondhandbackend.dto.RatingCommentResponse;
import com.example.secondhandbackend.dto.RatingCommentsPageResponse;
import com.example.secondhandbackend.dto.RatingSummaryResponse;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.entity.Rating;
import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.exception.DuplicateResourceException;
import com.example.secondhandbackend.exception.InvalidInputException;
import com.example.secondhandbackend.exception.ResourceNotFoundException;
import com.example.secondhandbackend.repository.AdvertisementRepository;
import com.example.secondhandbackend.repository.RatingRepository;
import com.example.secondhandbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository,
                         AdvertisementRepository advertisementRepository,
                         UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    public void submitRating(Long raterId, Long advertisementId, Integer score, String comment) {

        if (score == null || score < 1 || score > 5) {
            throw new InvalidInputException("Score must be between 1 and 5");
        }

        Advertisement ad = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        User seller = ad.getOwner();

        if (seller.getId().equals(raterId)) {
            throw new InvalidInputException("You cannot rate yourself");
        }

        if (ratingRepository.existsByAdvertisementIdAndRaterId(advertisementId, raterId)) {
            throw new DuplicateResourceException("You have already rated this advertisement");
        }

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Rating rating = new Rating();
        rating.setAdvertisement(ad);
        rating.setRater(rater);
        rating.setSeller(seller);
        rating.setScore(score);
        rating.setComment(comment);
        rating.setCreatedAt(LocalDateTime.now());

        ratingRepository.save(rating);
    }

    public RatingSummaryResponse getSellerRatings(Long sellerId) {

        Double average = ratingRepository.findAverageScoreBySellerId(sellerId);
        Long total = ratingRepository.countBySellerId(sellerId);

        double roundedAverage = average != null ? Math.round(average * 10.0) / 10.0 : 0.0;

        return new RatingSummaryResponse(roundedAverage, total);
    }

    /**
     * Returns the seller's text reviews, newest first.
     * If {@code limit} is null, all comments are returned (used by the
     * "see all reviews" page); otherwise only the first {@code limit}
     * comments are returned (used to show a short preview, e.g. 3 items),
     * alongside the true total count so the frontend knows whether a
     * "see all" option should be shown.
     */
    public RatingCommentsPageResponse getSellerComments(Long sellerId, Integer limit) {

        List<Rating> ratings = ratingRepository.findCommentsBySellerId(sellerId);
        long totalCount = ratingRepository.countCommentsBySellerId(sellerId);

        if (limit != null && limit >= 0 && ratings.size() > limit) {
            ratings = ratings.subList(0, limit);
        }

        List<RatingCommentResponse> comments = ratings.stream()
                .map(r -> new RatingCommentResponse(
                        r.getId(),
                        r.getScore(),
                        r.getComment(),
                        r.getRater().getFullName(),
                        r.getCreatedAt()))
                .toList();

        return new RatingCommentsPageResponse(comments, totalCount);
    }
}