package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.secondhandbackend.enums.AdStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    List<Advertisement> findByStatus(AdStatus status);
    boolean existsByCategoryId(Long categoryId);

    List<Advertisement> findByOwnerIdAndStatusNotOrderByCreatedAtDesc(Long ownerId, AdStatus excludedStatus);

    @Query("""
        SELECT a FROM Advertisement a
        WHERE a.status = com.example.secondhandbackend.enums.AdStatus.ACTIVE
        AND (:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR a.category.id = :categoryId)
        AND (:cityId IS NULL OR a.city.id = :cityId)
        AND (:minPrice IS NULL OR a.price >= :minPrice)
        AND (:maxPrice IS NULL OR a.price <= :maxPrice)
        ORDER BY
            CASE WHEN :sort = 'price_asc' THEN a.price END ASC,
            CASE WHEN :sort = 'price_desc' THEN a.price END DESC,
            a.createdAt DESC
        """)
    List<Advertisement> searchAdvertisements(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("cityId") Long cityId,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("sort") String sort
    );
}