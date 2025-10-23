package com.translateai.entity.domain;

import com.translateai.constant.classconstant.EntityProperties;
import com.translateai.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "data_configuration")
public class DataConfigurationEntity extends PrimaryEntity {

    @Column(length = EntityProperties.LENGTH_ID)
    String upId;

    @Column(length = EntityProperties.LENGTH_CODE)
    String category;

    @Column(length = EntityProperties.LENGTH_NAME)
    @Nationalized
    String name;

    @Column(length = Integer.MAX_VALUE)
    @Nationalized
    String description;

    @Column(length = Integer.MAX_VALUE)
    @Nationalized
    String value;

    Integer status;

    @Column(length = EntityProperties.LENGTH_CODE)
    String role;
}
