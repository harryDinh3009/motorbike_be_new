package com.translateai.dto.business.admin.userMng;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMngListDTO {

    private Integer rowNum;

    private String id;

    private String userName;

    private String fullName;

    private String email;

    private String genderNm;

    private String roleNm;

    private String phoneNumber;

    private String statusNm;

    private String avatar;

}
