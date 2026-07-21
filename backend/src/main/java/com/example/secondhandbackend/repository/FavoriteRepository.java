package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserId(Long userId);

    boolean existsByUserIdAndAdvertisementId(Long userId, Long advertisementId);
}