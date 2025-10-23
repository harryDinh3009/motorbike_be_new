package com.translateai.dto.common.authenticate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;

    private String tokenType = "Bearer";

    private int expiresIn;

    private String username;

}
