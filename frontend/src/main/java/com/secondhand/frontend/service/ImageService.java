package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.http.MultipartApiClient;
import com.secondhand.frontend.model.UploadImageResponse;
import com.secondhand.frontend.util.JsonUtil;

import java.io.File;

public class ImageService {

    /** Uploads one image for the given advertisement. Owner-only, requires JWT (handled by MultipartApiClient). */
    public static UploadImageResponse upload(long advertisementId, File file) throws ApiException {
        String json = MultipartApiClient.uploadFile(
                "/api/advertisements/" + advertisementId + "/images", file, "file");
        return JsonUtil.fromJson(json, UploadImageResponse.class);
    }
}