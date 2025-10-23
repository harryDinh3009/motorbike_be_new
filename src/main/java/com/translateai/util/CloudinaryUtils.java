package com.translateai.util;

import com.translateai.common.ApiStatus;
import com.translateai.config.exception.RestApiException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author nguyencongthang2509
 */
public class CloudinaryUtils {

    public static String extractPublicId(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();

            String[] parts = path.split("/");
            if (parts.length > 0) {
                String publicIdWithExtension = parts[parts.length - 1];
                int dotIndex = publicIdWithExtension.lastIndexOf(".");
                if (dotIndex != -1) {
                    return publicIdWithExtension.substring(0, dotIndex);
                }
            }
        } catch (MalformedURLException e) {
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    public static String extractPublicIdFile(String urlFile) {
        try {
            URL url = new URL(urlFile);
            String path = url.getPath();

            String[] parts = path.split("/");
            if (parts.length > 0) {
                String publicIdWithExtension = parts[parts.length - 1];

                return publicIdWithExtension;
            }
        } catch (MalformedURLException e) {
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }
}