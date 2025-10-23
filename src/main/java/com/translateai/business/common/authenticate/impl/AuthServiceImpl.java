package com.translateai.business.common.authenticate.impl;

import com.translateai.business.common.authenticate.impl.facebook.FacebookAuthService;
import com.translateai.business.common.authenticate.impl.google.GoogleTokenVerifierService;
import com.translateai.business.common.authenticate.service.AuthService;
import com.translateai.business.common.service.service.CommonService;
import com.translateai.common.ApiStatus;
import com.translateai.common.Constants;
import com.translateai.config.exception.RestApiException;
import com.translateai.config.mail.EmailSender;
import com.translateai.config.security.jwt.JwtTokenProvider;
import com.translateai.dto.common.authenticate.FacebookUserInfoDTO;
import com.translateai.dto.common.authenticate.LoginFacebookDTO;
import com.translateai.dto.common.authenticate.LoginGoogleDTO;
import com.translateai.dto.common.authenticate.LoginRequestDTO;
import com.translateai.dto.common.authenticate.LoginResponseDTO;
import com.translateai.entity.domain.UserEntity;
import com.translateai.entity.system.RoleEntity;
import com.translateai.repository.business.admin.UserRepository;
import com.translateai.repository.system.RoleRepository;
import com.translateai.repository.system.UserRoleRepository;
import com.translateai.util.RandomStringGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final RoleRepository roleRepository;
    private final EmailSender emailSender;
    private final FacebookAuthService facebookAuthService;
    private final CommonService commonService;

    @Value("${app.jwtExpirationMs}")
    private Integer jwtExpirationMs;

    @Override
    public LoginResponseDTO loginBasicAdmin(@Valid LoginRequestDTO loginRequest) {
        return this.loginBasicWithRole(loginRequest, Constants.SITE_ADMIN);
    }

    @Override
    public LoginResponseDTO loginBasicClient(@Valid LoginRequestDTO loginRequest) {
        return this.loginBasicWithRole(loginRequest, Constants.SITE_CLIENT);
    }

    private LoginResponseDTO loginBasicWithRole(LoginRequestDTO loginRequest, String requiredRole) {
        log.info("[LoginBasic] Start login for username={}, requiredRole={}", loginRequest.getUsername(), requiredRole);
        UserEntity userEntityFind = userRepository.findByEmail(loginRequest.getUsername());
        if (Objects.isNull(userEntityFind)) {
            log.warn("[LoginBasic] User not found: {}", loginRequest.getUsername());
            throw new RestApiException(ApiStatus.USER_NOT_FOUND);
        }
        if (userEntityFind.getStatus().equals(Constants.CD_STATUS_02)) {
            log.warn("[LoginBasic] User not active: {}", loginRequest.getUsername());
            throw new RestApiException(ApiStatus.USER_NOT_ACTIVE);
        }
        List<RoleEntity> listRoleEntity = userRoleRepository.findByUser(userEntityFind.getId());
        boolean hasRequiredRole = listRoleEntity.stream()
                .anyMatch(roleEntity -> requiredRole.equals(roleEntity.getCategory()));
        if (!hasRequiredRole) {
            log.warn("[LoginBasic] No access permission for user: {}", loginRequest.getUsername());
            throw new RestApiException(ApiStatus.NO_ACCESS_PERMISSION);
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), userEntityFind.getPassword())) {
            log.warn("[LoginBasic] Invalid credentials for user: {}", loginRequest.getUsername());
            throw new RestApiException(ApiStatus.INVALID_CREDENTIALS);
        }
        String jwt = jwtTokenProvider.generateTokenUser(userEntityFind);
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken(jwt);
        loginResponseDTO.setUsername(loginRequest.getUsername());
        loginResponseDTO.setExpiresIn(jwtExpirationMs);
        log.info("[LoginBasic] Success login for username={}", loginRequest.getUsername());
        return loginResponseDTO;
    }

    @Override
    @Transactional
    public LoginResponseDTO loginGoogleClient(@Valid LoginGoogleDTO loginGoogleDTO) {
        log.info("[GoogleLogin] Start verify token for tokenId={}", loginGoogleDTO.getTokenId());
        UserEntity userVerify = googleTokenVerifierService.verifyGoogleToken(loginGoogleDTO.getTokenId());
        String email = userVerify.getEmail();

        log.info("[GoogleLogin] Start verify login for {}", email);

        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            log.info("[GoogleLogin] Auto sign-up new user with email={}", email);

            RoleEntity roleEntity = roleRepository.findByRlCd(Constants.CD_ROLE_CLIENT);
            if (roleEntity == null) {
                log.error("[GoogleLogin] Role not found for CD_ROLE_CLIENT");
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }

            userEntity = new UserEntity();
            userEntity.setEmail(email);
            userEntity.setFullName(userVerify.getFullName());
            userEntity.setAvatar(userVerify.getAvatar());
            userEntity.setUserName(userVerify.getUserName());
            userEntity.setStatus(Constants.CD_STATUS_01);

            String passwordRandom = RandomStringGenerator.generateRandomPassword();
            userEntity.setPassword(passwordEncoder.encode(passwordRandom));
            userEntity.setRoles(Collections.singletonList(roleEntity));

            userEntity = userRepository.save(userEntity);

            String htmlBody = "<p>Your password for login is:</p><br/><div style=\"text-align: center; font-weight: bold; font-size: 25px;\"><strong>" + passwordRandom + "</strong></div>";
            log.info("[GoogleLogin] Sending email to new user: {}", email);
            emailSender.sendEmail(new String[]{email}, "[VIET-FLOW] Login password notification", "Login password notification", htmlBody);

            log.info("[GoogleLogin] New Google user created: {}", userEntity.getEmail());
        } else {
            if (Constants.CD_STATUS_02.equals(userEntity.getStatus())) {
                log.warn("[GoogleLogin] User not active: {}", email);
                throw new RestApiException(ApiStatus.USER_NOT_ACTIVE);
            }

            List<RoleEntity> roles = userRoleRepository.findByUser(userEntity.getId());
            boolean hasClientRole = roles.stream()
                    .anyMatch(r -> Constants.SITE_CLIENT.equals(r.getCategory()));

            if (!hasClientRole) {
                log.warn("[GoogleLogin] No access permission for user: {}", email);
                throw new RestApiException(ApiStatus.NO_ACCESS_PERMISSION);
            }
        }

        String jwt = jwtTokenProvider.generateTokenUser(userEntity);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(jwt);
        response.setUsername(userEntity.getEmail());
        response.setExpiresIn(jwtExpirationMs);

        log.info("[GoogleLogin] Success login for {}", email);
        return response;
    }

    @Override
    @Transactional
    public LoginResponseDTO loginFacebookClient(@Valid LoginFacebookDTO loginFacebookDTO) {
        log.info("[FacebookLogin] Start verify token for accessToken={}", loginFacebookDTO.getAccessToken());
        FacebookUserInfoDTO facebookUserInfoDTO = facebookAuthService.verifyAndGetUser(loginFacebookDTO.getAccessToken());

        String facebookId = facebookUserInfoDTO.getId();
        log.info("[FacebookLogin] Start verify login for facebookId={}", facebookId);

        UserEntity userEntity = userRepository.findByFacebookId(facebookId);

        if (userEntity == null) {
            log.info("[FacebookLogin] Auto sign-up new user with facebookId={}", facebookId);

            RoleEntity roleEntity = roleRepository.findByRlCd(Constants.CD_ROLE_CLIENT);
            if (roleEntity == null) {
                log.error("[FacebookLogin] Role not found for CD_ROLE_CLIENT");
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }

            userEntity = new UserEntity();
            if (Objects.nonNull(facebookUserInfoDTO.getEmail())) {
                userEntity.setEmail(facebookUserInfoDTO.getEmail());
                userEntity.setUserName(facebookUserInfoDTO.getEmail().split("@")[0]);

                String passwordRandom = RandomStringGenerator.generateRandomPassword();
                userEntity.setPassword(passwordEncoder.encode(passwordRandom));

                String htmlBody = "<p>Your password for login is:</p><br/><div style=\"text-align: center; font-weight: bold; font-size: 25px;\"><strong>" + passwordRandom + "</strong></div>";
                log.info("[FacebookLogin] Sending email to new user: {}", facebookUserInfoDTO.getEmail());
                emailSender.sendEmail(new String[]{facebookUserInfoDTO.getEmail()}, "[VIET-FLOW] Login password notification", "Login password notification", htmlBody);
                log.info("[FacebookLogin] New Facebook user created: {}", facebookId);
            }

            userEntity.setFullName(facebookUserInfoDTO.getName());
            userEntity.setAvatar(facebookUserInfoDTO.getPictureUrl());
            userEntity.setFacebookId(facebookUserInfoDTO.getId());
            userEntity.setStatus(Constants.CD_STATUS_01);
            userEntity.setRoles(Collections.singletonList(roleEntity));
            userEntity = userRepository.save(userEntity);
        } else {
            if (Constants.CD_STATUS_02.equals(userEntity.getStatus())) {
                log.warn("[FacebookLogin] User not active: {}", facebookId);
                throw new RestApiException(ApiStatus.USER_NOT_ACTIVE);
            }

            List<RoleEntity> roles = userRoleRepository.findByUser(userEntity.getId());
            boolean hasClientRole = roles.stream()
                    .anyMatch(r -> Constants.SITE_CLIENT.equals(r.getCategory()));

            if (!hasClientRole) {
                log.warn("[FacebookLogin] No access permission for user: {}", facebookId);
                throw new RestApiException(ApiStatus.NO_ACCESS_PERMISSION);
            }
        }

        String jwt = jwtTokenProvider.generateTokenUser(userEntity);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(jwt);
        response.setUsername(userEntity.getEmail());
        response.setExpiresIn(jwtExpirationMs);

        log.info("[FacebookLogin] Success login for facebookId={}", facebookId);
        return response;
    }

}
