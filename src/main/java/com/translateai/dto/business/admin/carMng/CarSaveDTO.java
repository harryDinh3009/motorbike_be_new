package com.translateai.dto.business.admin.carMng;

import com.translateai.constant.enumconstant.CarStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class CarSaveDTO {

    /** ID xe (null khi tạo mới) */
    private String id;

    /** Mẫu xe */
    private String model;

    /** Biển số xe */
    private String licensePlate;

    /** Loại xe */
    private String carType;

    /** ID chi nhánh */
    private String branchId;

    /** Giá thuê theo ngày */
    private BigDecimal dailyPrice;

    /** Giá thuê theo giờ */
    private BigDecimal hourlyPrice;

    /** Tình trạng xe */
    private String condition;

    /** Odometer hiện tại */
    private Integer currentOdometer;

    /** Trạng thái */
    private CarStatus status;

    /** URL ảnh xe */
    private String imageUrl;

    /** Ghi chú */
    private String note;

    // Thông tin bổ sung
    /** Năm sản xuất */
    private Integer yearOfManufacture;

    /** Xuất xứ */
    private String origin;

    /** Giá trị xe */
    private BigDecimal value;

    /** Số khung */
    private String frameNumber;

    /** Số máy */
    private String engineNumber;

    /** Màu sắc */
    private String color;

    /** Số giấy đăng ký xe */
    private String registrationNumber;

    /** Tên trên đăng ký */
    private String registeredOwnerName;

    /** Nơi đăng ký */
    private String registrationPlace;

    /** Số hợp đồng bảo hiểm TNDS */
    private String insuranceContractNumber;

    /** Ngày hết hạn bảo hiểm TNDS */
    private Date insuranceExpiryDate;
}

