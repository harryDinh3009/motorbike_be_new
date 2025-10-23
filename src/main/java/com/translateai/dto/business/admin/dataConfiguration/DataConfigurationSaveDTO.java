package com.translateai.dto.business.admin.dataConfiguration;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataConfigurationSaveDTO {

    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String upId;

    private String role;

}
