package com.motorbikebe.dto.business.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDailyRevenueDTO {
    private LocalDate date;              // Ngày trong tháng
    private BigDecimal contractAmount;   // Doanh thu thực thu của ngày
    private BigDecimal rentalAmount;     // Doanh thu tiền thuê xe trong ngày
    private BigDecimal surchargeAmount;  // Doanh thu phụ thu trong ngày
    private BigDecimal totalAmount;      // Tổng doanh thu hiển thị per day
}

