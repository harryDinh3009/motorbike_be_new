package com.translateai.business.common.singUp.web;

import com.translateai.business.common.singUp.service.SingUpService;
import com.translateai.common.ApiResponse;
import com.translateai.common.ApiStatus;
import com.translateai.dto.common.authenticate.LoginResponseDTO;
import com.translateai.dto.common.singUp.SingUpBasicDTO;
import com.translateai.dto.common.singUp.SingUpGoogleDTO;
import com.translateai.dto.common.singUp.SingUpUserAuthCodeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sing-up")
@RequiredArgsConstructor
public class SingUpController {

    private final SingUpService singUpService;

    /**
     * Send Code To Email
     *
     * @param singUpUserAuthCodeDTO .
     * @return Boolean
     */
    @PostMapping("/send-code")
    public ApiResponse<Boolean> sendCodeToEmail(@RequestBody SingUpUserAuthCodeDTO singUpUserAuthCodeDTO) {
        return new ApiResponse<>(ApiStatus.SUCCESS, singUpService.sendCodeToEmail(singUpUserAuthCodeDTO));
    }

    /**
     * Sing Up Basic
     *
     * @param singUpBasicDTO .
     * @return Boolean
     */
    @PostMapping("/basic")
    public ApiResponse<Boolean> singUpBasic(@RequestBody SingUpBasicDTO singUpBasicDTO) {
        return new ApiResponse<>(ApiStatus.SUCCESS, singUpService.singUpBasic(singUpBasicDTO));
    }

    /**
     * Sing Up Google
     *
     * @param singUpGoogleDTO .
     * @return LoginResponseDTO
     */
    @PostMapping("/google")
    public ApiResponse<LoginResponseDTO> singUpGoogle(@RequestBody SingUpGoogleDTO singUpGoogleDTO) {
        return new ApiResponse<>(ApiStatus.SUCCESS, singUpService.singUpWithGoogle(singUpGoogleDTO));
    }

}
