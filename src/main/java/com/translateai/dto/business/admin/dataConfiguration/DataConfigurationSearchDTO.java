package com.translateai.dto.business.admin.dataConfiguration;

import com.translateai.dto.common.PageableDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataConfigurationSearchDTO extends PageableDTO {
    String name;

    Integer status;
}
