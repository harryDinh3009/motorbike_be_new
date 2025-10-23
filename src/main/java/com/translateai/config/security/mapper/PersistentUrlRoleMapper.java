package com.translateai.config.security.mapper;

import com.translateai.repository.system.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class PersistentUrlRoleMapper implements IUrlRoleMapper {

    private final LinkedHashMap<String, String> urlRoleMappings = new LinkedHashMap<>();

    private final ResourceRepository resourceRepository;

    @Override
    public Map<String, String> getUrlRoleMappings() {
        List<Map<String, Object>> allResources = resourceRepository.findAllResources();
        // Add all resource => urlRoleMappings
        allResources.forEach(re -> {
            String roleExpression = String.valueOf(re.get("roles"));

            roleExpression = String.join(" or ",
                    Arrays.stream(roleExpression.split(","))
                            .map(s -> s.trim().equals("permitAll") ? "permitAll" : "hasRole('" + s + "')")
                            .toArray(String[]::new));

            urlRoleMappings.put(String.valueOf(re.get("url")), roleExpression);
        });

        // PermitAll Url
        urlRoleMappings.put("/code/**", "permitAll");
        urlRoleMappings.put("/auth/**", "permitAll");
        urlRoleMappings.put("/sing-up/**", "permitAll");
        urlRoleMappings.put("/cmn/files/**", "permitAll");
        urlRoleMappings.put("/cmn/menu/**", "permitAll");
        urlRoleMappings.put("/cmn/forgot-pass/**", "permitAll");
        urlRoleMappings.put("/c/news-board/list", "permitAll");
        urlRoleMappings.put("/c/news-board/detail", "permitAll");
        urlRoleMappings.put("/c/comment/list", "permitAll");
        urlRoleMappings.put("/c/comment/list-child", "permitAll");
        urlRoleMappings.put("/c/hot-topic/**", "permitAll");
        urlRoleMappings.put("/c/search/**", "permitAll");
        urlRoleMappings.put("/c/my-profile/list-industry", "permitAll");
        urlRoleMappings.put("/c/my-profile/list-job-select", "permitAll");

        // TODO Authorization
//        urlRoleMappings.clear();
        urlRoleMappings.put("/**", "permitAll");

        return urlRoleMappings;
    }

}
