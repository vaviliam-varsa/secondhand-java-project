package com.example.secondhandbackend.dto;

public class RatingSummaryResponse {

    private Double averageScore;
    private Long totalRatings;

    public RatingSummaryResponse(Double averageScore, Long totalRatings) {
        this.averageScore = averageScore;
        this.totalRatings = totalRatings;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public Long getTotalRatings() {
        return totalRatings;
    }
}