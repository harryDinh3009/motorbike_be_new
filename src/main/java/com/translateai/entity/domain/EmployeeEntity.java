package com.translateai.entity.domain;

import com.translateai.constant.classconstant.EntityProperties;
import com.translateai.entity.base.PrimaryEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Nationalized;

import java.util.Date;

/**
 * Entity quản lý thông tin nhân viên
 */
@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
public class EmployeeEntity extends PrimaryEntity {

    /** Họ và tên nhân viên */
    @Column(name = "full_name", nullable = false, length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String fullName;

    /** Số điện thoại */
    @Column(name = "phone_number", nullable = false, length = EntityProperties.LENGTH_PHONE)
    private String phoneNumber;

    /** Email */
    @Column(name = "email", length = EntityProperties.LENGTH_EMAIL)
    private String email;

    /** Ngày sinh */
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    /** Giới tính */
    @Column(name = "gender", length = EntityProperties.LENGTH_CODE)
    private String gender;

    /** Địa chỉ */
    @Column(name = "address", length = 500)
    @Nationalized
    private String address;

    /** ID chi nhánh */
    @Column(name = "branch_id", length = EntityProperties.LENGTH_ID)
    private String branchId;

    /** Vai trò/Chức vụ */
    @Column(name = "role", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String role;

    /** Trạng thái (1: Đang làm, 0: Nghỉ việc) */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
}

