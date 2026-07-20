package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
}