package com.translateai.dto.business.admin.carMng;

import com.translateai.constant.enumconstant.CarStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CarDTO {

    /** ID xe */
    private String id;

    /** Mẫu xe */
    private String model;

    /** Biển số xe */
    private String licensePlate;

    /** Loại xe */
    private String carType;

    /** ID chi nhánh */
    private String branchId;

    /** Tên chi nhánh */
    private String branchName;

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

    /** Tên trạng thái */
    private String statusNm;

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

    /**
     * Constructor cho Spring Data JPA native query projection (24 parameters)
     * Note: status nhận String từ database và convert sang enum
     */
    public CarDTO(String id, String model, String licensePlate, String carType,
                  String branchId, String branchName, BigDecimal dailyPrice, BigDecimal hourlyPrice,
                  String condition, Integer currentOdometer, String status, String imageUrl,
                  String note, Integer yearOfManufacture, String origin, BigDecimal value,
                  String frameNumber, String engineNumber, String color, String registrationNumber,
                  String registeredOwnerName, String registrationPlace, String insuranceContractNumber,
                  Date insuranceExpiryDate) {
        this.id = id;
        this.model = model;
        this.licensePlate = licensePlate;
        this.carType = carType;
        this.branchId = branchId;
        this.branchName = branchName;
        this.dailyPrice = dailyPrice;
        this.hourlyPrice = hourlyPrice;
        this.condition = condition;
        this.currentOdometer = currentOdometer;
        // Convert String to CarStatus enum
        this.status = (status != null) ? CarStatus.valueOf(status) : null;
        this.imageUrl = imageUrl;
        this.note = note;
        this.yearOfManufacture = yearOfManufacture;
        this.origin = origin;
        this.value = value;
        this.frameNumber = frameNumber;
        this.engineNumber = engineNumber;
        this.color = color;
        this.registrationNumber = registrationNumber;
        this.registeredOwnerName = registeredOwnerName;
        this.registrationPlace = registrationPlace;
        this.insuranceContractNumber = insuranceContractNumber;
        this.insuranceExpiryDate = insuranceExpiryDate;
    }
}

