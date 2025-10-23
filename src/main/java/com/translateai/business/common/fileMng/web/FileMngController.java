package com.translateai.business.common.fileMng.web;

import com.translateai.common.ApiResponse;
import com.translateai.common.ApiStatus;
import com.translateai.config.cloudinary.CloudinaryUploadImages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cmn/files")
@RequiredArgsConstructor
public class FileMngController {

    private final CloudinaryUploadImages cloudinaryUploadImages;

    /**
     * upload All File
     *
     * @param files    .
     * @param category .
     * @return List<String>
     */
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<List<String>> uploadFile(@RequestParam("files") MultipartFile[] files,
                                                   @RequestParam("category") String category) {
        return new ResponseEntity<>(cloudinaryUploadImages.uploadMultipleImages(files, category), HttpStatus.CREATED);
    }

    /**
     * Upload Image Form Editor
     *
     * @param file .
     * @return Map<String, Object>
     */
    @PostMapping(value = "/upload/form-editor")
    public ResponseEntity<Map<String, Object>> uploadFileFormEditor(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        result.put("url", cloudinaryUploadImages.uploadImage(file, "form_editor"));
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Upload Image
     *
     * @param file .
     * @return String
     */
    @PostMapping (value = "/upload-image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        String imageUrl = cloudinaryUploadImages.uploadImage(file, type);
        return new ApiResponse<>(ApiStatus.CREATED, imageUrl);
    }

}
