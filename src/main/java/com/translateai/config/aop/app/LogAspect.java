package com.translateai.config.aop.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.translateai.common.Constants;
import com.translateai.config.security.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around ("execution(* com.translateai.business..*.*(..))")
    public Object logging(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = null;
//        HttpServletResponse response = null;

        // Check if there is an active HTTP request
        if (RequestContextHolder.getRequestAttributes() != null) {
            // Only proceed with request attributes if available
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//            response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        }

        long startTime = System.currentTimeMillis();
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext()
                .getAuthentication();

//        String paramsString = WebUtils.getArgumentsAsString(pjp.getArgs());

        if (Objects.nonNull(authentication) && authentication.getName().equals(Constants.ANONYMOUS_USER)) {
            log.info("==> LogAspect Before:: {}", pjp.getSignature().getDeclaringTypeName());
        } else {
            log.info("==> LogAspect Before:: [ {} ]", pjp.getSignature().getDeclaringTypeName());
        }

        Object result = pjp.proceed();

        String reqPayload = null;
        if (request != null) {
            reqPayload = logRequestPayloadSafely(request);
        }

        if (Objects.nonNull(authentication) && authentication.getName().equals(Constants.ANONYMOUS_USER)) {
            log.info("==> LogAspect After:: {}", pjp.getSignature().getDeclaringTypeName());
        } else {
            log.info("==> LogAspect After:: [ {} ]", pjp.getSignature().getDeclaringTypeName());
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        log.info("==> Execution Time: {} ms", executionTime);

        // Log response payload
        String resPayload = logResponsePayload(result);

        if (reqPayload != null && !reqPayload.isEmpty()) {
            log.info("==> Request Payload: {}", reqPayload);
        }
        if (resPayload != null && !resPayload.isEmpty()) {
            log.info("==> Response Payload: {}", resPayload);
        }

        return result;
    }

    private String logRequestPayloadSafely(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            } else {
                log.warn("Request is not wrapped with ContentCachingRequestWrapper. " +
                        "Cannot safely log request payload. Please add LoggingFilter to your configuration.");
            }
            return "";
        } catch (Exception e) {
            log.error("Error logging request payload: {}", e.getMessage());
            return "";
        }
    }

    private String logResponsePayload(Object response) {
        try {
            String resPayload = "";
            if (response != null) {
                if (response instanceof ResponseEntity<?> responseEntity) {
                    Object body = responseEntity.getBody();
                    if (body != null) {
                        return objectMapper.writeValueAsString(body);
                    }
                } else {
                    return objectMapper.writeValueAsString(response);
                }
            }
            return resPayload;
        } catch (Exception e) {
            log.error("Error logging response payload: {}", e.getMessage());
            return "";
        }
    }

}
