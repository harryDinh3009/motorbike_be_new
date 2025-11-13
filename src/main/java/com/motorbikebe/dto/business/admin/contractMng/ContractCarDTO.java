package com.motorbikebe.dto.business.admin.contractMng;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO hiển thị thông tin xe trong hợp đồng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractCarDTO {
    private String id;
    private String contractId;
    private String carId;
    
    // Thông tin từ Car
    private String carModel; // Tên xe
    private String carType; // Loại xe
    private String licensePlate; // Biển số
    
    // Giá thuê
    private BigDecimal dailyPrice;
    private BigDecimal hourlyPrice;
    private BigDecimal totalAmount;
    
    // Odometer
    private Integer startOdometer;
    private Integer endOdometer;
    
    private String notes;
    private String status;
}

