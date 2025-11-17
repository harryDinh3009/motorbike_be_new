package com.motorbikebe.dto.business.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDTO {
    private DashboardPerformanceDTO performance;          // Các chỉ số hiệu suất tổng quan
    private DashboardRevenueOverviewDTO revenueOverview;  // Khối doanh thu hôm nay/tháng này/tháng trước
    private List<DashboardDailyRevenueDTO> dailyRevenue;  // Dữ liệu biểu đồ doanh thu theo ngày
}

