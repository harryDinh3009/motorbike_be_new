package com.motorbikebe.dto.business.admin.carMng;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO hiển thị mẫu xe
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarModelDTO {
    private String id;
    private String name;
    private String brand;
    private String description;
    private Boolean active;
}

