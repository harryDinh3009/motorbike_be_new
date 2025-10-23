package com.translateai.dto.common.singUp;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingUpUserAuthCodeDTO {

    @NotBlank
    private String email;

}
