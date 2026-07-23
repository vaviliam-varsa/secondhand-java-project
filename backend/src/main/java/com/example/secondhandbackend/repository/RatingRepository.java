package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findBySellerId(Long sellerId);

    boolean existsByAdvertisementIdAndRaterId(Long advertisementId, Long raterId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.seller.id = :sellerId")
    Double findAverageScoreBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.seller.id = :sellerId")
    Long countBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT r FROM Rating r WHERE r.seller.id = :sellerId " +
            "AND r.comment IS NOT NULL AND r.comment <> '' " +
            "ORDER BY r.createdAt DESC")
    List<Rating> findCommentsBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.seller.id = :sellerId " +
            "AND r.comment IS NOT NULL AND r.comment <> ''")
    Long countCommentsBySellerId(@Param("sellerId") Long sellerId);
}