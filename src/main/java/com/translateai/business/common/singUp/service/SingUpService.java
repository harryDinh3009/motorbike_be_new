package com.translateai.business.common.singUp.service;

import com.translateai.dto.common.authenticate.LoginResponseDTO;
import com.translateai.dto.common.singUp.SingUpBasicDTO;
import com.translateai.dto.common.singUp.SingUpGoogleDTO;
import com.translateai.dto.common.singUp.SingUpUserAuthCodeDTO;
import jakarta.validation.Valid;

public interface SingUpService {

    /**
     * Sing Up Basic
     *
     * @param singUpBasicDTO .
     * @return Boolean
     */
    Boolean singUpBasic(@Valid SingUpBasicDTO singUpBasicDTO);

    /**
     * Send Code To Email
     *
     * @param singUpUserAuthCodeDTO .
     * @return Boolean
     */
    Boolean sendCodeToEmail(@Valid SingUpUserAuthCodeDTO singUpUserAuthCodeDTO);

    /**
     * Sing Up Google
     *
     * @param singUpGoogleDTO .
     * @return LoginResponseDTO
     */
    LoginResponseDTO singUpWithGoogle(@Valid SingUpGoogleDTO singUpGoogleDTO);

}
