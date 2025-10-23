package com.translateai.dto.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResourceDTO {
    private Long rsId;
    private String url;
    private String httpMethod;
    private String rsNm;
    private String rsType;
    private Long regDate;
}
