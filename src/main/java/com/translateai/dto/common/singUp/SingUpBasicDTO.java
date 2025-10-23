package com.translateai.dto.common.singUp;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingUpBasicDTO {

    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @NotBlank
    private String gender;

    @NotBlank
    private String dateOfBirth;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String rePassword;

    @NotBlank
    private String authCode;

}
