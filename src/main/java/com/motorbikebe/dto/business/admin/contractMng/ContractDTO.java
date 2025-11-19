package com.motorbikebe.dto.business.admin.contractMng;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.motorbikebe.constant.enumconstant.ContractStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * DTO hiển thị thông tin hợp đồng (đã nâng cấp)
 */
@Getter
@Setter
@NoArgsConstructor
public class ContractDTO {
    // Basic Info
    private String id; // ID hợp đồng (UUID trong bảng contract)
    private String contractCode; // Mã hợp đồng (ví dụ HD000123)
    private String customerId; // ID khách hàng gắn hợp đồng
    private String customerName; // Tên khách thuê
    private String phoneNumber; // Số điện thoại khách
    private String email; // Email khách
    private String country; // Quốc tịch khách (nếu có)
    private String citizenId; // CCCD/Hộ chiếu khách
    private String customerAddress; // Địa chỉ thường trú của khách
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+07:00")
    private Date customerDateOfBirth; // Ngày sinh của khách
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+07:00")
    private Date citizenIdIssuedDate; // Ngày cấp CCCD/CMND (nếu có)
    private Integer totalContracts; // Số hợp đồng thuê
    
    // Contract Info
    private String source; // Nguồn tạo hợp đồng (web, app, referral...)
    private Date startDate; // Ngày/giờ bắt đầu thuê
    private Date endDate; // Ngày/giờ kết thúc dự kiến
    private String pickupBranchId; // ID chi nhánh nhận xe
    private String pickupBranchName; // Tên chi nhánh nhận xe
    private String returnBranchId; // ID chi nhánh trả xe
    private String returnBranchName; // Tên chi nhánh trả xe
    private String pickupAddress; // Địa chỉ nhận xe thực tế
    private String returnAddress; // Địa chỉ trả xe thực tế
    private Boolean needPickupDelivery; // Có cần giao xe tận nơi khi nhận
    private Boolean needReturnDelivery; // Có cần nhận xe tận nơi khi trả
    private String notes; // Ghi chú nội bộ
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+07:00")
    private Date createdDate; // Thời gian tạo hợp đồng
    
    // Financial Info
    private BigDecimal totalRentalAmount; // Tổng tiền thuê xe gốc (trước phụ thu/giảm trừ)
    private BigDecimal totalSurcharge; // Tổng phụ thu đang áp cho hợp đồng
    private String discountType; // Loại giảm giá (PERCENT/FIXED)
    private BigDecimal discountValue; // Giá trị giảm (phần trăm hoặc số tiền)
    private BigDecimal discountAmount; // Tiền giảm thực tế sau tính toán
    private BigDecimal depositAmount; // Tiền cọc khách đã đặt
    private BigDecimal finalAmount; // Tổng phải thu cuối cùng sau giảm giá
    private BigDecimal paidAmount; // Số tiền khách đã thanh toán
    private BigDecimal remainingAmount; // Số tiền còn thiếu
    
    // Status
    private ContractStatus status; // Trạng thái hợp đồng (enum ContractStatus)
    private String statusNm; // Tên trạng thái để hiển thị
    
    // Delivery & Return Info
    private String deliveryUserId; // ID nhân viên giao xe
    private String deliveryUserName; // Tên nhân viên giao xe
    private Date deliveryTime; // Thời gian giao xe thực tế
    private String returnUserId; // ID nhân viên nhận xe
    private String returnUserName; // Tên nhân viên nhận xe
    private Date returnTime; // Thời gian nhận xe thực tế
    private Date completedDate; // Ngày hoàn tất hợp đồng
    
    // Relationships (load khi cần)
    private List<ContractCarDTO> cars; // Danh sách xe gắn với hợp đồng
    private List<SurchargeDTO> surcharges; // Danh sách phụ thu đính kèm
    private List<PaymentTransactionDTO> payments; // Lịch sử thanh toán của hợp đồng
    private List<ContractImageDTO> deliveryImages; // Ảnh chụp khi giao xe
    private List<ContractImageDTO> returnImages; // Ảnh chụp khi nhận xe
    
    /**
     * Constructor for native query projection (26 parameters)
     * Must match the SELECT columns in ContractRepository.searchContracts()
     */
    public ContractDTO(String id, String contractCode, String customerId, String customerName,
                       String phoneNumber, String email, String source, Date startDate, Date endDate,
                       String pickupBranchId, String pickupBranchName, String returnBranchId,
                       String returnBranchName, String pickupAddress, String returnAddress,
                       BigDecimal totalRentalAmount, BigDecimal totalSurcharge, BigDecimal discountAmount,
                       BigDecimal finalAmount, BigDecimal paidAmount, BigDecimal remainingAmount,
                       String status, String notes, Date deliveryTime, Date returnTime, Date completedDate) {
        this.id = id;
        this.contractCode = contractCode;
        this.customerId = customerId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.source = source;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pickupBranchId = pickupBranchId;
        this.pickupBranchName = pickupBranchName;
        this.returnBranchId = returnBranchId;
        this.returnBranchName = returnBranchName;
        this.pickupAddress = pickupAddress;
        this.returnAddress = returnAddress;
        this.totalRentalAmount = totalRentalAmount;
        this.totalSurcharge = totalSurcharge;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = remainingAmount;
        // Convert String status to enum
        if (status != null && !status.isEmpty()) {
            this.status = ContractStatus.valueOf(status);
            this.statusNm = this.status.getDescription();
        }
        this.notes = notes;
        this.deliveryTime = deliveryTime;
        this.returnTime = returnTime;
        this.completedDate = completedDate;
    }
}
