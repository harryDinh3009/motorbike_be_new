package com.motorbikebe.dto.business.admin.carMng;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AvailableCarReportRequestDTO {
    private Date startDate;
    private Date endDate;
    private String branchId;
    private String modelName;
    private String carType;
}

