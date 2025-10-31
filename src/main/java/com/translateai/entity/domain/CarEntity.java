package com.translateai.entity.domain;

import com.translateai.constant.classconstant.EntityProperties;
import com.translateai.constant.enumconstant.CarStatus;
import com.translateai.entity.base.PrimaryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity quản lý thông tin xe
 */
@Entity
@Table(name = "car")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
public class CarEntity extends PrimaryEntity {

    /** Mẫu xe */
    @Column(name = "model", nullable = false, length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String model;

    /** Biển số xe */
    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    /** Loại xe (Xe số, Xe ga, Xe cao cấo, Xe tay côn) */
    @Column(name = "car_type", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String carType;

    /** ID chi nhánh sở hữu */
    @Column(name = "branch_id", length = EntityProperties.LENGTH_ID)
    private String branchId;

    /** Giá thuê theo ngày */
    @Column(name = "daily_price", precision = 15, scale = 2)
    private BigDecimal dailyPrice;

    /** Giá thuê theo giờ */
    @Column(name = "hourly_price", precision = 15, scale = 2)
    private BigDecimal hourlyPrice;

    /** Tình trạng xe (Nguyên vẹn, Hỏng hóc) */
    @Column(name = "condition", length = EntityProperties.LENGTH_CODE)
    @Nationalized
    private String condition;

    /** Odometer hiện tại (số km đã đi) */
    @Column(name = "current_odometer")
    private Integer currentOdometer;

    /** Trạng thái (Hoạt động, Không sẵn sàng, Bị mất) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CarStatus status = CarStatus.AVAILABLE;

    /** URL hình ảnh xe */
    @Column(name = "image_url", length = EntityProperties.LENGTH_URL)
    private String imageUrl;

    /** Ghi chú */
    @Lob
    @Column(name = "note")
    @Nationalized
    private String note;

    // ===== Thông tin bổ sung =====

    /** Năm sản xuất */
    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    /** Xuất xứ */
    @Column(name = "origin", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String origin;

    /** Giá trị xe */
    @Column(name = "value", precision = 15, scale = 2)
    private BigDecimal value;

    /** Số khung */
    @Column(name = "frame_number", length = 50)
    private String frameNumber;

    /** Số máy */
    @Column(name = "engine_number", length = 50)
    private String engineNumber;

    /** Màu sắc */
    @Column(name = "color", length = EntityProperties.LENGTH_CODE)
    @Nationalized
    private String color;

    /** Số giấy đăng ký xe */
    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    /** Tên trên đăng ký */
    @Column(name = "registered_owner_name", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String registeredOwnerName;

    /** Nơi đăng ký */
    @Column(name = "registration_place", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String registrationPlace;

    /** Số hợp đồng bảo hiểm TNDS */
    @Column(name = "insurance_contract_number", length = 50)
    private String insuranceContractNumber;

    /** Ngày hết hạn bảo hiểm TNDS */
    @Column(name = "insurance_expiry_date")
    @Temporal(TemporalType.DATE)
    private Date insuranceExpiryDate;
}

