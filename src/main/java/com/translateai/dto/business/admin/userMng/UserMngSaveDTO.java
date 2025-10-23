package com.translateai.dto.business.admin.userMng;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMngSaveDTO {

    private String id;

    private String username;

    private String fullName;

    private String email;

    private String roleCd;

    private String genderCd;

    private String phoneNumber;

    private String dateOfBirth;

    private String statusCd;

}
