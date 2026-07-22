package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.AdminPendingAd;
import com.secondhand.frontend.model.AdminRejectRequest;
import com.secondhand.frontend.model.AdminUser;
import com.secondhand.frontend.util.JsonUtil;

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
}