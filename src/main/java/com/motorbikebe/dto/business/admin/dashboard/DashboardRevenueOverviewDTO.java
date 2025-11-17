package com.motorbikebe.dto.business.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRevenueOverviewDTO {
    private DashboardRevenueBlockDTO today;      // Doanh thu hôm nay
    private DashboardRevenueBlockDTO thisMonth;  // Doanh thu tháng hiện tại
    private DashboardRevenueBlockDTO lastMonth;  // Doanh thu tháng trước
}

