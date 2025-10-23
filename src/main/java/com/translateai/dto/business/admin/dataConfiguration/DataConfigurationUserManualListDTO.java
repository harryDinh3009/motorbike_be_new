package com.translateai.dto.business.admin.dataConfiguration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author HoangDV
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataConfigurationUserManualListDTO {
    Integer rowNum;

    String id;

    String name;

    String code;

    String description;

    Integer status;
}
