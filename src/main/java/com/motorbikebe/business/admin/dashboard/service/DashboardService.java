package com.motorbikebe.business.admin.dashboard.service;

import com.motorbikebe.dto.business.admin.dashboard.DashboardResponseDTO;

public interface DashboardService {
    DashboardResponseDTO getDashboard(String branchId);
}

