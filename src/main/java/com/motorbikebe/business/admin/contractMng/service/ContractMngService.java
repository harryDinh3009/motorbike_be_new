package com.motorbikebe.business.admin.contractMng.service;

import com.motorbikebe.common.PageableObject;
import com.motorbikebe.dto.business.admin.contractMng.*;
import com.motorbikebe.entity.domain.ContractEntity;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service quản lý hợp đồng (đã nâng cấp)
 */
public interface ContractMngService {

    /**
     * Tìm kiếm hợp đồng với phân trang
     */
    PageableObject<ContractDTO> searchContracts(ContractSearchDTO searchDTO);

    /**
     * Lấy chi tiết hợp đồng
     */
    ContractDTO getContractDetail(String id);

    /**
     * Tạo mới hoặc cập nhật hợp đồng
     */
    ContractEntity saveContract(@Valid ContractSaveDTO saveDTO);

    /**
     * Xóa hợp đồng
     */
    Boolean deleteContract(String id);

    // ========== Contract Cars ==========
    
    /**
     * Lấy danh sách xe trong hợp đồng
     */
    List<ContractCarDTO> getContractCars(String contractId);

    // ========== Surcharges ==========
    
    /**
     * Thêm phụ thu cho hợp đồng
     */
    Boolean addSurcharge(@Valid SurchargeSaveDTO saveDTO);

    /**
     * Xóa phụ thu
     */
    Boolean deleteSurcharge(String id);

    /**
     * Lấy danh sách phụ thu theo hợp đồng
     */
    List<SurchargeDTO> getSurchargesByContractId(String contractId);

    // ========== Payments ==========
    
    /**
     * Thêm thanh toán cho hợp đồng
     */
    Boolean addPayment(@Valid PaymentTransactionSaveDTO saveDTO);

    /**
     * Xóa thanh toán
     */
    Boolean deletePayment(String id);

    /**
     * Lấy lịch sử thanh toán
     */
    List<PaymentTransactionDTO> getPaymentHistory(String contractId);

    // ========== Delivery & Return ==========
    
    /**
     * Cập nhật thông tin giao xe
     */
    Boolean updateDelivery(@Valid ContractDeliveryDTO deliveryDTO);

    /**
     * Upload ảnh giao xe
     */
    List<String> uploadDeliveryImages(String contractId, List<MultipartFile> files);

    /**
     * Cập nhật thông tin nhận xe
     */
    Boolean updateReturn(@Valid ContractReturnDTO returnDTO);

    /**
     * Upload ảnh nhận xe
     */
    List<String> uploadReturnImages(String contractId, List<MultipartFile> files);

    // ========== Complete Contract ==========
    
    /**
     * Đóng hợp đồng (hoàn thành thanh toán)
     */
    Boolean completeContract(@Valid ContractCompleteDTO completeDTO);

    // ========== PDF Generation ==========
    
    /**
     * Tải xuống file PDF hợp đồng
     */
    byte[] downloadContractPDF(String id);
}
