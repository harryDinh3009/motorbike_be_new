package com.translateai.business.common.forgotPassword.web;

import com.translateai.business.common.forgotPassword.service.ForgotPasswordService;
import com.translateai.common.ApiResponse;
import com.translateai.common.ApiStatus;
import com.translateai.dto.common.forgotPassword.ForgotPasswordRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cmm/forgot-pass")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping
    public ApiResponse<Boolean> forgotPassword(@RequestBody ForgotPasswordRequestDTO req) {
        return new ApiResponse<>(ApiStatus.SUCCESS, forgotPasswordService.forgotPassword(req));
    }

}
