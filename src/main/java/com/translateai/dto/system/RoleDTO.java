package com.translateai.dto.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoleDTO {
    private Long rlId;
    private String rlName;
    private String category;
    private String etc;
    private String rlCd;
    private String rlDesc;
}
