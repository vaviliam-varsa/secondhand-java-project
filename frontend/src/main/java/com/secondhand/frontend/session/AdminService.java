package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.AdminPendingAd;
import com.secondhand.frontend.model.AdminRejectRequest;
import com.secondhand.frontend.model.AdminUser;
import com.secondhand.frontend.util.JsonUtil;
import com.secondhand.frontend.model.AdminCategoryRequest;
import com.secondhand.frontend.model.Category;

import java.util.List;

public class AdminService {

    public static List<AdminPendingAd> pendingAds() throws ApiException {
        String json = ApiClient.get("/api/admin/advertisements/pending", true);
        return JsonUtil.fromJsonList(json, AdminPendingAd.class);
    }

    public static void approve(long adId) throws ApiException {
        ApiClient.put("/api/admin/advertisements/" + adId + "/approve", null, true);
    }

    public static void reject(long adId, String reason) throws ApiException {
        AdminRejectRequest req = new AdminRejectRequest();
        req.reason = reason;
        ApiClient.put("/api/admin/advertisements/" + adId + "/reject", req, true);
    }

    public static List<AdminUser> users() throws ApiException {
        String json = ApiClient.get("/api/admin/users", true);
        return JsonUtil.fromJsonList(json, AdminUser.class);
    }

    public static void block(long userId) throws ApiException {
        ApiClient.put("/api/admin/users/" + userId + "/block", null, true);
    }

    public static void unblock(long userId) throws ApiException {
        ApiClient.put("/api/admin/users/" + userId + "/unblock", null, true);
    }

    public static List<Category> categories() throws ApiException {
        String json = ApiClient.get("/api/admin/categories", true);
        return JsonUtil.fromJsonList(json, Category.class);
    }

    public static Category createCategory(String name) throws ApiException {
        AdminCategoryRequest req = new AdminCategoryRequest();
        req.name = name;
        String json = ApiClient.post("/api/admin/categories", req, true);
        return JsonUtil.fromJson(json, Category.class);
    }

    public static Category updateCategory(long id, String name) throws ApiException {
        AdminCategoryRequest req = new AdminCategoryRequest();
        req.name = name;
        String json = ApiClient.put("/api/admin/categories/" + id, req, true);
        return JsonUtil.fromJson(json, Category.class);
    }

    public static void deleteCategory(long id) throws ApiException {
        ApiClient.delete("/api/admin/categories/" + id, true);
    }
}