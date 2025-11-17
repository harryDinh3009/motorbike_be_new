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
public class DashboardPerformanceDTO {
    private long totalContracts; // Tổng số hợp đồng trong kỳ lọc
    private long totalCars;      // Tổng số xe đang cho thuê (lọc theo chi nhánh)
    private BigDecimal totalRevenue; // Tổng doanh thu tháng hiện tại
}

