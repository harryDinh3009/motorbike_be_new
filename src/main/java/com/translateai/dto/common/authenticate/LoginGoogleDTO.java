package com.translateai.dto.common.authenticate;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginGoogleDTO {

    @NotBlank
    private String tokenId;

}
