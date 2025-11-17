package com.motorbikebe.entity.domain;

import com.motorbikebe.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Danh mục mẫu xe dùng cho các nghiệp vụ
 */
@Entity
@Table(name = "car_model")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarModelEntity extends PrimaryEntity {

    @Column(name = "name", nullable = false, unique = true, length = 150)
    String name;

    @Column(name = "brand", length = 100)
    String brand;

    @Column(name = "description", length = 500)
    String description;

    @Column(name = "is_active")
    Boolean active;
}

