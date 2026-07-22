package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.LoginRequest;
import com.secondhand.frontend.model.LoginResponse;
import com.secondhand.frontend.model.RegisterRequest;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.util.JsonUtil;

public class AuthService {

    public static void register(RegisterRequest req) throws ApiException {
        ApiClient.post("/api/auth/register", req, false);
    }

    public static LoginResponse login(String username, String password) throws ApiException {
        LoginRequest req = new LoginRequest();
        req.username = username;
        req.password = password;

        String json = ApiClient.post("/api/auth/login", req, false);
        LoginResponse resp = JsonUtil.fromJson(json, LoginResponse.class);
        SessionManager.getInstance().login(resp.token, resp.userId, resp.username, resp.role);
        return resp;
    }

    public static void logout() {
        SessionManager.getInstance().logout();
    }
}