package com.motorbikebe.dto.business.admin.carMng;

import com.motorbikebe.constant.enumconstant.CarStatus;
import com.motorbikebe.dto.common.PageableDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CarSearchDTO extends PageableDTO {

    /** Tìm kiếm theo mẫu xe, biển số */
    private String keyword;

    /** Lọc theo chi nhánh */
    private String branchId;

    /** Lọc theo mẫu xe cụ thể */
    private String modelName;

    /** Lọc theo loại xe */
    private String carType;

    /** Lọc theo tình trạng xe */
    private String condition;

    /** Lọc theo trạng thái */
    private CarStatus status;

    /** Ngày thuê (dùng cho API list-available) */
    private Date startDate;

    /** Ngày trả (dùng cho API list-available) */
    private Date endDate;
}

