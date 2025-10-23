package com.translateai.dto.common.authenticate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacebookUserInfoDTO {
    private String id;
    private String name;
    private String email;
    private String pictureUrl;
}
