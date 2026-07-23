package com.example.secondhandbackend.dto;

import java.util.List;

public class RatingCommentsPageResponse {

    private List<RatingCommentResponse> comments;
    private long totalCount;

    public RatingCommentsPageResponse(List<RatingCommentResponse> comments, long totalCount) {
        this.comments = comments;
        this.totalCount = totalCount;
    }

    public List<RatingCommentResponse> getComments() {
        return comments;
    }

    public long getTotalCount() {
        return totalCount;
    }
}