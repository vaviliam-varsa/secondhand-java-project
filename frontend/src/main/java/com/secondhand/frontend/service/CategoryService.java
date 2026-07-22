package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.util.JsonUtil;

import java.util.List;

public class CategoryService {
    public static List<Category> list() throws ApiException {
        String json = ApiClient.get("/api/categories", false);
        return JsonUtil.fromJsonList(json, Category.class);
    }
}