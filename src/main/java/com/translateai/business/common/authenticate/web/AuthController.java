package com.translateai.business.common.authenticate.web;

import com.translateai.business.common.authenticate.service.AuthService;
import com.translateai.common.ApiResponse;
import com.translateai.common.ApiStatus;
import com.translateai.dto.common.authenticate.LoginFacebookDTO;
import com.translateai.dto.common.authenticate.LoginGoogleDTO;
import com.translateai.dto.common.authenticate.LoginRequestDTO;
import com.translateai.dto.common.authenticate.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login Admin Basic
     *
     * @param loginRequestDTO .
     * @return ApiResponse<LoginResponseDTO>
     */
    @PostMapping("/login/a/b")
    public ApiResponse<LoginResponseDTO> loginBasicAdmin(@RequestBody LoginRequestDTO loginRequestDTO) {
        return new ApiResponse<>(ApiStatus.SUCCESS, authService.loginBasicAdmin(loginRequestDTO));
    }

    /**
     * Login Client Basic
     *
     * @param loginRequestDTO .
     * @return ApiResponse<LoginResponseDTO>
     */
    @PostMapping("/login/c/b")
    public ApiResponse<LoginResponseDTO> loginBasicClient(@RequestBody LoginRequestDTO loginRequestDTO) {
        return new ApiResponse<>(ApiStatus.SUCCESS, authService.loginBasicClient(loginRequestDTO));
    }

    /**
     * Login Google Client
     *
     * @param loginGoogleDTO .
     * @return LoginResponseDTO
     */
    @PostMapping("/login/c/g")
    public ApiResponse<LoginResponseDTO> loginGoogleClient(@RequestBody LoginGoogleDTO loginGoogleDTO) {
        return new ApiResponse<>(ApiStatus.SUCCESS, authService.loginGoogleClient(loginGoogleDTO));
    }

    /**
     * Login Facebook Client
     *
     * @param loginFacebookDTO .
     * @return LoginResponseDTO
     */
    @PostMapping("/login/c/f")
    public ApiResponse<LoginResponseDTO> loginFacebookClient(@RequestBody LoginFacebookDTO loginFacebookDTO) {
        return new ApiResponse<>(ApiStatus.SUCCESS, authService.loginFacebookClient(loginFacebookDTO));
    }

    /**
     * Verify Token Page Feed
     *
     * @return Boolean
     */
    @GetMapping("/verify-token")
    public ApiResponse<Boolean> verifyToken() {
        return new ApiResponse<>(ApiStatus.SUCCESS, Boolean.TRUE);
    }

}
