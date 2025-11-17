package com.motorbikebe.dto.business.admin.carMng;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Tên mẫu xe không được để trống")
    private String name;

    private String brand;

    private String description;

    private Boolean active;
}

