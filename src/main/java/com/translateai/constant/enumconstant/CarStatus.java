package com.translateai.constant.enumconstant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CarStatus {
    AVAILABLE("Hoạt động"),
    NOT_AVAILABLE("Không sẵn sàng"),
    LOST("Bị mất"),
    RENTED("Đang cho thuê"),
    MAINTENANCE("Đang bảo dưỡng");

    private final String description;
}

