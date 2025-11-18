package com.motorbikebe.business.admin.carMng.service;

import com.motorbikebe.dto.business.admin.carMng.AvailableCarReportRequestDTO;

public interface CarReportService {
    byte[] exportAvailableCarsReport(AvailableCarReportRequestDTO request);
}

