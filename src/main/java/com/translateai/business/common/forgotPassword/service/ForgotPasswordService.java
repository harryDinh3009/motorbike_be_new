package com.translateai.business.common.forgotPassword.service;

import com.translateai.dto.common.forgotPassword.ForgotPasswordRequestDTO;
import jakarta.validation.Valid;

public interface ForgotPasswordService {

    /**
     * Forgot Password
     *
     * @param forgotPasswordRequestDTO .
     * @return boolean
     */
    boolean forgotPassword(@Valid ForgotPasswordRequestDTO forgotPasswordRequestDTO);

}
