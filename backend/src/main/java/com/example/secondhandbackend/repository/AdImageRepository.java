package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.AdImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdImageRepository extends JpaRepository<AdImage, Long> {
}