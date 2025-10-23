package com.translateai.dto.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResourceRoleDTO {
    private Long rrId;
    private Long rsId;
    private Long rlId;
    private String url;
    private String resourceRoles;
}
