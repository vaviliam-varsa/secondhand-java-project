package com.example.secondhandbackend.dto;

import java.time.LocalDateTime;

public class RatingCommentResponse {

    private Long id;
    private Integer score;
    private String comment;
    private String raterName;
    private LocalDateTime createdAt;

    public RatingCommentResponse(Long id, Integer score, String comment, String raterName, LocalDateTime createdAt) {
        this.id = id;
        this.score = score;
        this.comment = comment;
        this.raterName = raterName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Integer getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public String getRaterName() {
        return raterName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}