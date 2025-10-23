package com.translateai.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.translateai.dto.system.ResultDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component("restFailureHandler")
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        int startIndex = exception.toString().indexOf("userId : ");
        String userId = "";
        if (startIndex != -1) {
            userId = exception.toString().substring(startIndex + "userId : ".length());
        }

        if (exception instanceof BadCredentialsException) {
            mapper.writeValue(response.getWriter(), "Invalid username or password");
        }

        mapper.writeValue(response.getWriter(), new ResultDTO<>(null, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase()));
    }
}
