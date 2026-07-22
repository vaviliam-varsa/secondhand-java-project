package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.AddFavoriteRequest;
import com.secondhand.frontend.model.FavoriteItem;
import com.secondhand.frontend.util.JsonUtil;

import java.util.List;

public class FavoriteService {

    public static List<FavoriteItem> list() throws ApiException {
        String json = ApiClient.get("/api/favorites", true);
        return JsonUtil.fromJsonList(json, FavoriteItem.class);
    }

    /** Adds an advertisement to the current user's favorites. */
    public static void add(long advertisementId) throws ApiException {
        AddFavoriteRequest req = new AddFavoriteRequest();
        req.advertisementId = advertisementId;
        ApiClient.post("/api/favorites", req, true);
    }

    /** Removes a favorite entry. Note: {id} here is the favorite's own id, not the advertisement id. */
    public static void remove(long favoriteId) throws ApiException {
        ApiClient.delete("/api/favorites/" + favoriteId, true);
    }
}