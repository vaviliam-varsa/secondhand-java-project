package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.AdImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdImageRepository extends JpaRepository<AdImage, Long> {

    List<AdImage> findByAdvertisementId(Long advertisementId);
}