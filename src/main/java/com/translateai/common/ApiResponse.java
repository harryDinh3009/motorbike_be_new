package com.translateai.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ApiResponse<T> extends ResponseEntity<Map<String, Object>> {

    private String code;

    private String message;

    private T data;

    public ApiResponse(ApiStatus apiStatus, T data) {
        super(createBody(apiStatus, data), createHeaders(), apiStatus.getHttpStatus());
        this.code = apiStatus.getCode();
        this.message = apiStatus.getMessage();
        this.data = data;
    }

    public ApiResponse(ApiStatus apiStatus, T data, String customMessage) {
        super(createBody(apiStatus, data, customMessage), createHeaders(), apiStatus.getHttpStatus());
        this.code = apiStatus.getCode();
        this.message = customMessage;
        this.data = data;
    }

    private static <T> Map<String, Object> createBody (ApiStatus apiStatus, T data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", apiStatus.getCode());
        body.put("message", apiStatus.getMessage());
        body.put("data", data);
        return body;
    }

    private static <T> Map<String, Object> createBody (ApiStatus apiStatus, T data, String customMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", apiStatus.getCode());
        body.put("message", customMessage);
        body.put("data", data);
        return body;
    }

    private static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
