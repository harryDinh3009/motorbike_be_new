package com.translateai.dto.business.admin.userMng;

import com.translateai.dto.common.PageableDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMngSearchDTO extends PageableDTO {

    private String fullName;

    private String email;

    private String role;

    private String gender;

    private String phoneNumber;

    private String status;

}
