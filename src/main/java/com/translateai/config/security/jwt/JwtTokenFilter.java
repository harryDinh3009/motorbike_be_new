package com.translateai.config.security.jwt;

import com.translateai.config.security.custom.UserDetailsImpl;
import com.translateai.config.security.custom.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        String jwtToken = extractJwtToken(request);
        response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (jwtToken == null) {
            if (isApiAuthRequest(request)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (jwtToken != null) {
            if (jwtTokenProvider.validateToken(jwtToken)) {
                String email = jwtTokenProvider.getEmailFromToken(jwtToken);
                UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(email);
                Authentication authentication = jwtTokenProvider.getAuthentication(jwtToken, userDetails, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization token is required");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isApiAuthRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api")
                || request.getRequestURI().startsWith("/api/auth")
                || request.getRequestURI().startsWith("/api/code")
                || request.getRequestURI().startsWith("/api/sing-up")
                || request.getRequestURI().startsWith("/api/cmn/files")
                || request.getRequestURI().startsWith("/api/cmm/forgot-pass")
                || request.getRequestURI().startsWith("/api/cmm/menu")
                ;
    }

    private String extractJwtToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

}
