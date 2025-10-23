package com.translateai.config.security.manager;

import com.translateai.config.security.mapper.PersistentUrlRoleMapper;
import com.translateai.config.security.service.DynamicAuthorizationService;
import com.translateai.repository.system.ResourceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcherEntry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomDynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private volatile List<RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>> mappings;
    private static final AuthorizationDecision DENY = new AuthorizationDecision(false);
    private static final AuthorizationDecision ACCESS = new AuthorizationDecision(true);

    private final HandlerMappingIntrospector handlerMappingIntrospector;
    private final ResourceRepository resourceRepository;
    private DynamicAuthorizationService dynamicAuthorizationService;

    @PostConstruct
    public void mapping() {
        dynamicAuthorizationService =
                new DynamicAuthorizationService(new PersistentUrlRoleMapper(resourceRepository));

        setMapping();
    }

    private void setMapping() {
        Map<String, String> urlRoleMappings = dynamicAuthorizationService.getUrlRoleMappings();
        mappings = urlRoleMappings.entrySet().stream()
                .map(entry -> new RequestMatcherEntry<>(
                        new MvcRequestMatcher(handlerMappingIntrospector, entry.getKey()),
                        createAuthorizationManager(entry.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext request) {
        return mappings.stream()
                .filter(entry -> entry.getRequestMatcher().matches(request.getRequest()))
                .findFirst()
                .map(entry -> entry.getEntry().check(authentication, request))
                .orElse(DENY);
    }

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }

    private AuthorizationManager<RequestAuthorizationContext> createAuthorizationManager(String role) {
        return role.startsWith("ROLE")
                ? AuthorityAuthorizationManager.hasRole(role.substring(5))
                : new WebExpressionAuthorizationManager(role);
    }

    public synchronized void reload() {
        mappings.clear();
        setMapping();
    }

}
