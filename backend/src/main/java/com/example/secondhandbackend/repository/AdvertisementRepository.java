package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.secondhandbackend.enums.AdStatus;
import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    List<Advertisement> findByStatus(AdStatus status);
}