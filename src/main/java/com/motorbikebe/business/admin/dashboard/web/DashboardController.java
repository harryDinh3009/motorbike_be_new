package com.motorbikebe.business.admin.dashboard.web;

import com.motorbikebe.business.admin.dashboard.service.DashboardService;
import com.motorbikebe.common.ApiResponse;
import com.motorbikebe.common.ApiStatus;
import com.motorbikebe.dto.business.admin.dashboard.DashboardResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/a/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardResponseDTO> getDashboard(@RequestParam(value = "branchId", required = false) String branchId) {
        DashboardResponseDTO response = dashboardService.getDashboard(branchId);
        return new ApiResponse<>(ApiStatus.SUCCESS, response);
    }
}

