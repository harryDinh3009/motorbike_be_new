package com.translateai.business.common.forgotPassword.impl;

import com.translateai.business.common.forgotPassword.service.ForgotPasswordService;
import com.translateai.common.ApiStatus;
import com.translateai.common.Constants;
import com.translateai.config.exception.RestApiException;
import com.translateai.config.mail.EmailSender;
import com.translateai.dto.common.forgotPassword.ForgotPasswordRequestDTO;
import com.translateai.entity.domain.UserAuthCodeEntity;
import com.translateai.entity.domain.UserEntity;
import com.translateai.repository.business.admin.UserRepository;
import com.translateai.repository.business.client.UserAuthCodeRepository;
import com.translateai.util.RandomStringGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Validated
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserAuthCodeRepository userAuthCodeRepository;

    private final UserRepository userRepository;

    private final EmailSender emailSender;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public boolean forgotPassword(ForgotPasswordRequestDTO req) {
        UserEntity userEntityFind = userRepository.findByEmailAndStatus(req.getEmail(), Constants.CD_STATUS_01);
        if (Objects.isNull(userEntityFind)) {
            throw new RestApiException(ApiStatus.USER_NOT_FOUND);
        }
        if (Objects.nonNull(req.getOldPassword())) {
            if (!passwordEncoder.matches(req.getOldPassword(), userEntityFind.getPassword())) {
                throw new RestApiException(ApiStatus.INVALID_OLD_PASSWORD);
            }
        }
        if (! Objects.nonNull(req.getOtp())) {
            CompletableFuture.runAsync(() -> {
                String otp = RandomStringGenerator.generateOTPForUser();
                String htmlBody = "<p>Mã OTP của bạn là:</p><br/><div style=\"text-align: center; font-weight: bold; font-size: 25px;\"><strong>" + otp + "</strong></div>";
                emailSender.sendEmail(new String[] {req.getEmail()}, "[VIET-FLOW] Thông báo mã OTP",
                        "Thông báo mã " + "OTP sau khi xác thực email", htmlBody);
                UserAuthCodeEntity userAuthCodeFind = userAuthCodeRepository.findByEmail(req.getEmail());
                if (Objects.nonNull(userAuthCodeFind)) {
                    userAuthCodeFind.setCode(otp);
                    userAuthCodeRepository.save(userAuthCodeFind);
                } else {
                    UserAuthCodeEntity userAuthCode = new UserAuthCodeEntity();
                    userAuthCode.setCode(otp);
                    userAuthCode.setEmail(req.getEmail());
                    userAuthCodeRepository.save(userAuthCode);
                }
            }).exceptionally(ex -> {
                throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
            });
            return false;
        }
        UserAuthCodeEntity userAuthCodeEntityFind = userAuthCodeRepository.findByEmail(req.getEmail());
        Instant lastModifiedInstant = Instant.ofEpochMilli(userAuthCodeEntityFind.getLastModifiedDate());
        Duration duration = Duration.between(lastModifiedInstant, Instant.now());
        if (duration.getSeconds() > 120) throw new RestApiException(ApiStatus.AUTH_CODE_EXPIRED);
        if (! Objects.equals(userAuthCodeEntityFind.getCode(), req.getOtp()))
            throw new RestApiException(ApiStatus.INVALID_USER_AUTH_CODE);
        userEntityFind.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(userEntityFind);
        return true;
    }

}
