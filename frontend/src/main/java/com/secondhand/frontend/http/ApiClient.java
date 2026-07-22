package com.secondhand.frontend.http;

import com.secondhand.frontend.config.ApiConfig;
import com.secondhand.frontend.model.ApiError;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.util.JsonUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClient {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static String get(String path, boolean auth) throws ApiException {
        return request("GET", path, null, auth);
    }

    public static String getWithQuery(String path, Map<String, String> params, boolean auth) throws ApiException {
        String query = buildQuery(params);
        return request("GET", path + query, null, auth);
    }

    public static String post(String path, Object body, boolean auth) throws ApiException {
        return request("POST", path, body, auth);
    }

    public static String put(String path, Object body, boolean auth) throws ApiException {
        return request("PUT", path, body, auth);
    }

    public static String delete(String path, boolean auth) throws ApiException {
        return request("DELETE", path, null, auth);
    }

    private static String buildQuery(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getValue() == null || e.getValue().isBlank()) continue;
            if (!first) sb.append("&");
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }

    private static String request(String method, String path, Object body, boolean auth) throws ApiException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + path));

            if (auth) {
                String token = SessionManager.getInstance().getToken();
                if (token != null) {
                    builder.header("Authorization", "Bearer " + token);
                }
            }

            String json = body != null ? JsonUtil.toJson(body) : "";

            switch (method) {
                case "GET" -> builder.GET();
                case "POST" -> builder.header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
                case "PUT" -> builder.header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
                case "DELETE" -> builder.DELETE();
                default -> throw new IllegalArgumentException("متد نامعتبر: " + method);
            }

            HttpResponse<String> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return response.body();
            }

            String message = "خطای نامشخص از سرور";
            try {
                ApiError error = JsonUtil.fromJson(response.body(), ApiError.class);
                if (error.message != null) message = error.message;
            } catch (Exception ignored) {
                // پاسخ خطا فرمت استاندارد نداشت
            }
            throw new ApiException(status, message);

        } catch (IOException | InterruptedException e) {
            throw new ApiException(0, "اتصال به سرور برقرار نشد. مطمئن شوید Backend روی " + ApiConfig.BASE_URL + " در حال اجراست.");
        }
    }
}