package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.util.JsonUtil;

import java.util.List;

public class CityService {
    public static List<City> list() throws ApiException {
        String json = ApiClient.get("/api/cities", false);
        return JsonUtil.fromJsonList(json, City.class);
    }
}