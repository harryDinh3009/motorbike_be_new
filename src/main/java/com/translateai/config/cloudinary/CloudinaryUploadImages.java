package com.translateai.config.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.translateai.common.ApiStatus;
import com.translateai.config.exception.RestApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nguyencongthang2509
 */
@Service
public class CloudinaryUploadImages {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folderName) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "auto"
            );
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return (String) uploadResult.get("url");
        } catch (Exception e) {
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<String> uploadMultipleImages(MultipartFile[] files, String folderName) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Map<String, Object> params = ObjectUtils.asMap(
                        "folder", folderName,
                        "resource_type", "auto"
                );
                Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
                String url = (String) uploadResult.get("url");
                urls.add(url);
            } catch (Exception e) {
                throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return urls;
    }

    public String deleteImage(String publicId) {
        try {
            Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            if (deleteResult.containsKey("result") && deleteResult.get("result").equals("ok")) {
                return "Image deleted successfully";
            } else {
                throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

