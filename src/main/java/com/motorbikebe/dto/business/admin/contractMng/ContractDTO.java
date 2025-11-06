package com.motorbikebe.dto.business.admin.contractMng;

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
    private String id;
    private String contractCode;
    private String customerId;
    private String customerName;
    private String phoneNumber;
    private String email;
    private String country;
    private String citizenId;
    private Integer totalContracts; // Số hợp đồng thuê
    
    // Contract Info
    private String source; // Nguồn
    private Date startDate;
    private Date endDate;
    private String pickupBranchId;
    private String pickupBranchName;
    private String returnBranchId;
    private String returnBranchName;
    private String pickupAddress;
    private String returnAddress;
    private Boolean needPickupDelivery;
    private Boolean needReturnDelivery;
    private String notes;
    private Date createdDate;
    
    // Financial Info
    private BigDecimal totalRentalAmount;
    private BigDecimal totalSurcharge;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal depositAmount;
    private BigDecimal finalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    
    // Status
    private ContractStatus status;
    private String statusNm; // Tên trạng thái để hiển thị
    
    // Delivery & Return Info
    private String deliveryUserId;
    private String deliveryUserName;
    private Date deliveryTime;
    private String returnUserId;
    private String returnUserName;
    private Date returnTime;
    private Date completedDate;
    
    // Relationships (load khi cần)
    private List<ContractCarDTO> cars; // Danh sách xe
    private List<SurchargeDTO> surcharges; // Danh sách phụ thu
    private List<PaymentTransactionDTO> payments; // Lịch sử thanh toán
    private List<ContractImageDTO> deliveryImages; // Ảnh giao xe
    private List<ContractImageDTO> returnImages; // Ảnh nhận xe
    
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
