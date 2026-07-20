package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}