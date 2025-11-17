package com.motorbikebe.dto.business.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRevenueBlockDTO {
    private BigDecimal contractAmount;   // Tổng doanh thu đã thanh toán (final_amount)
    private BigDecimal rentalAmount;     // Doanh thu tiền thuê xe (trước phụ thu/giảm giá)
    private BigDecimal surchargeAmount;  // Tổng tiền phụ thu
    private BigDecimal totalAmount;      // Tổng hợp hiển thị (thường dùng contractAmount)
}

