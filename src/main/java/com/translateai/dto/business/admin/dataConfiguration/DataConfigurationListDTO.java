package com.translateai.dto.business.admin.dataConfiguration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataConfigurationListDTO {

    Integer rowNum;

    String id;

    String name;

    String description;

    String nameUp;

    String idUp;

    String roleNm;

    Long createdDate;

}
