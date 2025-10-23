package com.translateai.dto.business.admin.dataConfiguration;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author HoangDV
 */
@Getter
@Setter
@Builder
public class DataConfigurationJobListDTO {

    private String industryId;

    private String industryName;
}
