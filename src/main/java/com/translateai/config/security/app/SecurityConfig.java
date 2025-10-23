package com.translateai.config.security.app;

import com.translateai.config.security.custom.UserDetailsServiceImpl;
import com.translateai.config.security.entrypoint.RestAuthenticationEntryPoint;
import com.translateai.config.security.filter.RestAuthenticationFilter;
import com.translateai.config.security.handler.RestAccessDeniedHandler;
import com.translateai.config.security.handler.RestAuthenticationFailureHandler;
import com.translateai.config.security.handler.RestAuthenticationSuccessHandler;
import com.translateai.config.security.jwt.JwtTokenFilter;
import com.translateai.config.security.jwt.JwtTokenProvider;
import com.translateai.config.security.manager.CustomDynamicAuthorizationManager;
import com.translateai.constant.classconstant.CorsConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final RestAuthenticationSuccessHandler restSuccessHandler;
    private final RestAuthenticationFailureHandler restFailureHandler;
    private final CustomDynamicAuthorizationManager authorizationManager;

    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsServiceImpl userDetailsService,
            RestAuthenticationSuccessHandler restSuccessHandler,
            RestAuthenticationFailureHandler restFailureHandler,
            CustomDynamicAuthorizationManager authorizationManager
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.restSuccessHandler = restSuccessHandler;
        this.restFailureHandler = restFailureHandler;
        this.authorizationManager = authorizationManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtTokenFilter jwtTokenFilter = new JwtTokenFilter(jwtTokenProvider, userDetailsService);
        http
                // .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Tắt CORS
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(restAuthenticationFilter(http.getSharedObject(AuthenticationManager.class), http),
                        UsernamePasswordAuthenticationFilter.class)
                // Static Authorization
//                .authorizeHttpRequests(
//                        authorize -> authorize
//                        .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
//                        // TODO Authentication, Authorization
//                        .requestMatchers(new AntPathRequestMatcher("/api/**")).permitAll()
//                        .anyRequest()
//                        .authenticated()
//                )
                // Dynamic Authorization
                .authorizeHttpRequests(authorize -> authorize
                        //.requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers
                        .xssProtection(xss ->
                                xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(cps ->
                                cps.policyDirectives("script-src 'self'; object-src 'none'; base-uri 'none'"))
                        .httpStrictTransportSecurity(hsts ->
                                hsts.includeSubDomains(true).maxAgeInSeconds(31536000).preload(true))
                        .addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"))
                        .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                        .addHeaderWriter(new StaticHeadersWriter("Referrer-Policy", "no-referrer"))
                        .addHeaderWriter(new StaticHeadersWriter("X-Permitted-Cross-Domain-Policies", "none"))
                        .addHeaderWriter(new StaticHeadersWriter("X-Download-Options", "noopen"))
                        .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "default-src 'self'"))
                        .frameOptions().deny()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAccessDeniedHandler()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(CorsConstant.LIST_DOMAIN_ACCEPT);
        configuration.setAllowedMethods(CorsConstant.LIST_METHOD_ACCEPT);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private RestAuthenticationFilter restAuthenticationFilter(AuthenticationManager authenticationManager,
                                                              HttpSecurity http) {

        RestAuthenticationFilter restAuthenticationFilter = new RestAuthenticationFilter(http);
        restAuthenticationFilter.setAuthenticationManager(authenticationManager);
        restAuthenticationFilter.setAuthenticationSuccessHandler(restSuccessHandler);
        restAuthenticationFilter.setAuthenticationFailureHandler(restFailureHandler);

        return restAuthenticationFilter;
    }
}
