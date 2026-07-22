package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.SellerRatingsSummary;
import com.secondhand.frontend.model.SubmitRatingRequest;
import com.secondhand.frontend.util.JsonUtil;

public class RatingService {

    /** Submits a rating for the seller of the given advertisement. Requires JWT. */
    public static void submit(long advertisementId, int score, String comment) throws ApiException {
        SubmitRatingRequest req = new SubmitRatingRequest();
        req.advertisementId = advertisementId;
        req.score = score;
        req.comment = comment;
        ApiClient.post("/api/ratings", req, true);
    }

    /** Public endpoint - no auth required. */
    public static SellerRatingsSummary getSellerRatings(long userId) throws ApiException {
        String json = ApiClient.get("/api/users/" + userId + "/ratings", false);
        return JsonUtil.fromJson(json, SellerRatingsSummary.class);
    }
}