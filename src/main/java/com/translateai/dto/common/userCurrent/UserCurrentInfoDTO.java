package com.translateai.dto.common.userCurrent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class UserCurrentInfoDTO {

    private String id;

    private String email;

    private String password;

    private String fullName;

    private String userName;

    private String gender;

    private Date dateOfBirth;

    private String phoneNumber;

    private String avatar;

    private String description;

    private String myReferCode;

    private String status;

    private List<UserCurrentRoleDTO> roles;

}
