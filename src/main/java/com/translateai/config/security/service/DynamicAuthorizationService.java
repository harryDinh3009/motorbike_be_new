package com.translateai.config.security.service;

import com.translateai.config.security.mapper.IUrlRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class DynamicAuthorizationService {

    private final IUrlRoleMapper iUrlRoleMapper;

    public Map<String, String> getUrlRoleMappings() {
        return iUrlRoleMapper.getUrlRoleMappings();
    }

}
