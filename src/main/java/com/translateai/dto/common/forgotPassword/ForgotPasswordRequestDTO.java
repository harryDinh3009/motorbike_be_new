package com.translateai.dto.common.forgotPassword;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequestDTO {

    private String email;

    private String password;

    private String otp;

    private String oldPassword;

}
