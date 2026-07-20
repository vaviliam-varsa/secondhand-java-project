package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
}