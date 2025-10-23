package com.translateai.dto.common.user;

import java.util.Date;

/**
 * Projection interface for User Profile query
 */
public interface UserProfileProjection {
    String getEmail();
    String getFullName();
    String getUserName();
    String getGenderCd();
    String getGenderName();
    Date getDateOfBirth();
    String getPhoneNumber();
    String getAvatar();
    String getDescription();
    String getStatusCd();
    String getStatusName();
}
