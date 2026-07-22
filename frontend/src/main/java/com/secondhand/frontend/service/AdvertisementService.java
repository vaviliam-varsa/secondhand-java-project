package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.Advertisement;
import com.secondhand.frontend.model.AdvertisementDetail;
import com.secondhand.frontend.model.CreateAdvertisementRequest;
import com.secondhand.frontend.model.CreateAdvertisementResponse;
import com.secondhand.frontend.model.UpdateAdvertisementRequest;
import com.secondhand.frontend.util.JsonUtil;

import java.util.List;
import java.util.Map;

public class AdvertisementService {

    public static List<Advertisement> list(Map<String, String> filters) throws ApiException {
        String json = ApiClient.getWithQuery("/api/advertisements", filters, false);
        return JsonUtil.fromJsonList(json, Advertisement.class);
    }

    public static AdvertisementDetail getDetail(long id) throws ApiException {
        String json = ApiClient.get("/api/advertisements/" + id, false);
        return JsonUtil.fromJson(json, AdvertisementDetail.class);
    }

    /**
     * Creates a new advertisement. Requires JWT. The new ad is saved with status PENDING
     * and will not appear in the public list until an admin approves it.
     * Returns the new advertisement's id.
     */
    public static Long create(CreateAdvertisementRequest req) throws ApiException {
        String json = ApiClient.post("/api/advertisements", req, true);
        CreateAdvertisementResponse resp = JsonUtil.fromJson(json, CreateAdvertisementResponse.class);
        return resp.id;
    }

    /** Updates title/description/price. Owner only, requires JWT. */
    public static void update(long id, UpdateAdvertisementRequest req) throws ApiException {
        ApiClient.put("/api/advertisements/" + id, req, true);
    }

    /** Deletes an advertisement. Owner only, requires JWT. */
    public static void delete(long id) throws ApiException {
        ApiClient.delete("/api/advertisements/" + id, true);
    }

    /** Marks an advertisement as sold. Requires JWT. */
    public static void markSold(long id) throws ApiException {
        ApiClient.put("/api/advertisements/" + id + "/sold", null, true);
    }
}