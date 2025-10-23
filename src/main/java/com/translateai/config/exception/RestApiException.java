package com.translateai.config.exception;

import com.translateai.common.ApiStatus;
import lombok.Getter;

@Getter
public class RestApiException extends RuntimeException {

    private final ApiStatus apiStatus;

    public RestApiException(ApiStatus apiStatus) {
        super(apiStatus.getMessage());
        this.apiStatus = apiStatus;
    }

}
