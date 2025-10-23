package com.translateai.dto.common.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

/**
 * DTO for Update User Information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserDTO {

    @NotBlank(message = "Email is required")
    String email;
    
    String fullName;
    
    String userName;
    
    String gender;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date dateOfBirth;
    
    String phoneNumber;
    
    String avatar;
    
    String description;
    
}
