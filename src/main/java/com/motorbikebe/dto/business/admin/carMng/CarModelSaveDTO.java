package com.motorbikebe.dto.business.admin.carMng;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO tạo/cập nhật mẫu xe
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarModelSaveDTO {

    private String name;

    private String brand;

    private String description;

    private Boolean active;
}

