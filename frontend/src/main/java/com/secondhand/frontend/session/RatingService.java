package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.RatingCommentsPage;
import com.secondhand.frontend.model.SellerRatingsSummary;
import com.secondhand.frontend.model.SubmitRatingRequest;
import com.secondhand.frontend.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class RatingService {

    public static void submit(long advertisementId, int score, String comment) throws ApiException {
        SubmitRatingRequest req = new SubmitRatingRequest();
        req.advertisementId = advertisementId;
        req.score = score;
        req.comment = comment;
        ApiClient.post("/api/ratings", req, true);
    }

    public static SellerRatingsSummary getSellerRatings(long userId) throws ApiException {
        String json = ApiClient.get("/api/users/" + userId + "/ratings", false);
        return JsonUtil.fromJson(json, SellerRatingsSummary.class);
    }

    /** Pass null limit to fetch all comments. Public endpoint - no auth required. */
    public static RatingCommentsPage getSellerComments(long userId, Integer limit) throws ApiException {
        Map<String, String> params = new HashMap<>();
        if (limit != null) {
            params.put("limit", String.valueOf(limit));
        }
        String json = ApiClient.getWithQuery("/api/users/" + userId + "/ratings/comments", params, false);
        return JsonUtil.fromJson(json, RatingCommentsPage.class);
    }
}