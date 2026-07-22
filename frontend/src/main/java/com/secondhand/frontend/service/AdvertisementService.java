package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.Advertisement;
import com.secondhand.frontend.model.AdvertisementDetail;
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
}