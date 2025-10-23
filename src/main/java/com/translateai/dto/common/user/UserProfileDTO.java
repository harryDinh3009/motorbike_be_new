package com.translateai.dto.common.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

/**
 * DTO for User Profile Information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileDTO {

    String email;
    
    String fullName;
    
    String userName;
    
    String genderCd;
    String genderName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date dateOfBirth;
    
    String phoneNumber;
    
    String avatar;
    
    String description;
    
    String statusCd;
    
    String statusName;
    
    /**
     * Thông tin gói dịch vụ của user
     */
    SubscriptionInfoDTO subscriptionInfo;
    
}
