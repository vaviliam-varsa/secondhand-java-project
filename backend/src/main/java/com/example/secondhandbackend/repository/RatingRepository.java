package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findBySellerId(Long sellerId);

    boolean existsByAdvertisementIdAndRaterId(Long advertisementId, Long raterId);
}