package com.translateai.dto.common.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * DTO for Subscription Information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionInfoDTO {

    /**
     * Tên gói dịch vụ
     */
    String planTitle;
    
    /**
     * Mô tả gói dịch vụ
     */
    String planDescription;
    
    /**
     * Product ID của gói
     */
    String planProductId;
    
    /**
     * Tier của gói (Free, Paid_3M, Paid_1Y, etc.)
     */
    String planTier;
    
    /**
     * Thời gian kích hoạt gói (mua gói)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant purchaseTime;
    
    /**
     * Thời gian hết hạn gói
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant expiryTime;
    
    /**
     * Trạng thái subscription (ACTIVE, EXPIRED, CANCELED, etc.)
     */
    String status;
    
    /**
     * Quota còn lại
     */
    Long remainingQuota;
    
    /**
     * Có tự động gia hạn không
     */
    Boolean autoRenewing;
}

