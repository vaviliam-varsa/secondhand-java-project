package com.secondhand.frontend.http;

import com.secondhand.frontend.config.ApiConfig;
import com.secondhand.frontend.model.ApiError;
import com.secondhand.frontend.session.SessionManager;
import com.secondhand.frontend.util.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Minimal multipart/form-data uploader for endpoints like
 * POST /api/advertisements/{id}/images (field name "file").
 * java.net.http.HttpClient has no built-in multipart support, so the body is built by hand.
 */
public class MultipartApiClient {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static String uploadFile(String path, File file, String fieldName) throws ApiException {
        String boundary = "----SecondhandBoundary" + UUID.randomUUID();

        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String contentType = guessContentType(file);

            ByteArrayOutputStream body = new ByteArrayOutputStream();
            writeString(body, "--" + boundary + "\r\n");
            writeString(body, "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\""
                    + file.getName() + "\"\r\n");
            writeString(body, "Content-Type: " + contentType + "\r\n\r\n");
            body.write(fileBytes);
            writeString(body, "\r\n--" + boundary + "--\r\n");

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + path))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()));

            String token = SessionManager.getInstance().getToken();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
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
                // فرمت خطا استاندارد نبود
            }
            throw new ApiException(status, message);

        } catch (IOException | InterruptedException e) {
            throw new ApiException(0, "آپلود عکس ناموفق بود: اتصال به سرور برقرار نشد.");
        }
    }

    private static String guessContentType(File file) {
        try {
            String detected = Files.probeContentType(file.toPath());
            if (detected != null) return detected;
        } catch (IOException ignored) {
            // ادامه با حدس بر اساس پسوند
        }
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }

    private static void writeString(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.UTF_8));
    }
}