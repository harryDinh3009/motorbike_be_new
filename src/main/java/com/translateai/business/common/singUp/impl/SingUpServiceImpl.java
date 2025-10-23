package com.translateai.business.common.singUp.impl;

import com.translateai.business.common.authenticate.impl.google.GoogleTokenVerifierService;
import com.translateai.business.common.service.service.CommonService;
import com.translateai.business.common.singUp.service.SingUpService;
import com.translateai.common.ApiStatus;
import com.translateai.common.Constants;
import com.translateai.config.exception.RestApiException;
import com.translateai.config.mail.EmailSender;
import com.translateai.config.security.jwt.JwtTokenProvider;
import com.translateai.constant.classconstant.ScreenConstants;
import com.translateai.dto.common.authenticate.LoginResponseDTO;
import com.translateai.dto.common.singUp.SingUpBasicDTO;
import com.translateai.dto.common.singUp.SingUpGoogleDTO;
import com.translateai.dto.common.singUp.SingUpUserAuthCodeDTO;
import com.translateai.entity.domain.UserAuthCodeEntity;
import com.translateai.entity.domain.UserEntity;
import com.translateai.entity.system.RoleEntity;
import com.translateai.entity.system.UserRoleEntity;
import com.translateai.repository.business.admin.UserRepository;
import com.translateai.repository.business.client.UserAuthCodeRepository;
import com.translateai.repository.system.RoleRepository;
import com.translateai.repository.system.UserRoleRepository;
import com.translateai.util.RandomStringGenerator;
import com.translateai.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Validated
public class SingUpServiceImpl implements SingUpService {

    private final UserRepository userRepository;
    private final UserAuthCodeRepository userAuthCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final CommonService commonService;

    @Value("${app.jwtExpirationMs}")
    private Integer jwtExpirationMs;

    @Override
    @Transactional
    public Boolean singUpBasic(@Valid SingUpBasicDTO singUpBasicDTO) {
        UserAuthCodeEntity userAuthCodeEntity = userAuthCodeRepository.findByEmail(singUpBasicDTO.getEmail());
        if (Objects.isNull(userAuthCodeEntity)) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        if (!userAuthCodeEntity.getCode().equals(singUpBasicDTO.getAuthCode())) {
            throw new RestApiException(ApiStatus.INVALID_USER_AUTH_CODE);
        }
        LocalDateTime lastModifiedDate = Instant.ofEpochMilli(userAuthCodeEntity.getLastModifiedDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(lastModifiedDate, now);
        if (duration.toMinutes() > 2) {
            throw new RestApiException(ApiStatus.AUTH_CODE_EXPIRED);
        }

        UserEntity userEntityFindByEmail = userRepository.findByEmail(singUpBasicDTO.getEmail());
        if (Objects.nonNull(userEntityFindByEmail)) {
            throw new RestApiException(ApiStatus.EMAIL_EXIST);
        }

        UserEntity userEntityFindByUserName = userRepository.findByEmail(singUpBasicDTO.getUsername());
        if (Objects.nonNull(userEntityFindByUserName)) {
            throw new RestApiException(ApiStatus.USERNAME_EXIST);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setFullName(singUpBasicDTO.getFullName());
        userEntity.setEmail(singUpBasicDTO.getEmail());
        userEntity.setUserName(singUpBasicDTO.getUsername());
        userEntity.setPassword(passwordEncoder.encode(singUpBasicDTO.getPassword()));
        userEntity.setGender(singUpBasicDTO.getGender());
        userEntity.setDateOfBirth(Utils.convertStringToDate(singUpBasicDTO.getDateOfBirth(), "yyyy-MM-dd"));
        userEntity.setStatus(Constants.CD_STATUS_01);
        userEntity.setAvatar(ScreenConstants.URL_AVATAR_DEFAULT);

        UserEntity userEntitySave = userRepository.save(userEntity);

        RoleEntity roleEntity = roleRepository.findByRlCd(Constants.CD_ROLE_CLIENT);
        if (Objects.isNull(roleEntity)) throw new RestApiException(ApiStatus.NOT_FOUND);

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUserId(userEntitySave.getId());
        userRoleEntity.setRlId(roleEntity.getRlId());
        userRoleRepository.save(userRoleEntity);

        return true;
    }

    @Override
    @Transactional
    public Boolean sendCodeToEmail(@Valid SingUpUserAuthCodeDTO singUpUserAuthCodeDTO) {
        String email = singUpUserAuthCodeDTO.getEmail();
        UserAuthCodeEntity userAuthCodeEntity = userAuthCodeRepository.findByEmail(email);
        String randomCode = RandomStringGenerator.generateRandomNumericString();
        if (Objects.isNull(userAuthCodeEntity)) {
            userAuthCodeEntity = new UserAuthCodeEntity();
            userAuthCodeEntity.setEmail(email);
        }
        userAuthCodeEntity.setCode(randomCode);
        userAuthCodeRepository.save(userAuthCodeEntity);

        String htmlBody = "<p>Your verification code is:</p><br/><div style=\"text-align: center; font-weight: bold; font-size: 25px;\"><strong>" + randomCode + "</strong></div>";
        emailSender.sendEmail(new String[]{singUpUserAuthCodeDTO.getEmail()}, "[VIET-FLOW] Account verification code notification", "Account verification code notification", htmlBody);

        return true;
    }

    @Override
    @Transactional
    public LoginResponseDTO singUpWithGoogle(@Valid SingUpGoogleDTO singUpGoogleDTO) {
        if (!singUpGoogleDTO.getRole().equals(Constants.CD_ROLE_CLIENT) && !singUpGoogleDTO.getRole().equals(Constants.CD_ROLE_MENTOR)) {
            throw new RestApiException(ApiStatus.NO_ACCESS_PERMISSION);
        }
        UserEntity userVerify = googleTokenVerifierService.verifyGoogleToken(singUpGoogleDTO.getTokenId());
        UserEntity userEntityFind = userRepository.findByEmail(userVerify.getEmail());
        if (Objects.nonNull(userEntityFind)) {
            throw new RestApiException(ApiStatus.EMAIL_EXIST);
        }
        String passwordRandom = RandomStringGenerator.generateRandomPassword();
        userVerify.setPassword(passwordEncoder.encode(passwordRandom));
        userVerify.setStatus(Constants.CD_STATUS_01);
        RoleEntity roleEntity = roleRepository.findByRlCd(singUpGoogleDTO.getRole());
        if (Objects.isNull(roleEntity)) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        List<RoleEntity> roles = new ArrayList<>();
        roles.add(roleEntity);
        userVerify.setRoles(roles);
        UserEntity userEntitySave = userRepository.save(userVerify);

        String jwt = jwtTokenProvider.generateTokenUser(userEntitySave);
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken(jwt);
        loginResponseDTO.setUsername(userEntitySave.getEmail());
        loginResponseDTO.setExpiresIn(jwtExpirationMs);
        this.sendEmailAsync(userEntitySave.getEmail(), passwordRandom);
        return loginResponseDTO;
    }

    public void sendEmailAsync(String email, String passwordRandom) {
        String htmlBody = "<p>Your password for login is:</p><br/><div style=\"text-align: center; font-weight: bold; font-size: 25px;\"><strong>" + passwordRandom + "</strong></div>";
        emailSender.sendEmail(new String[]{email}, "[VIET-FLOW] Login password notification", "Login password notification", htmlBody);
    }

}
