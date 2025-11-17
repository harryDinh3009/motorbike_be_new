package com.motorbikebe.repository.business.admin;

import com.motorbikebe.constant.enumconstant.ContractStatus;
import com.motorbikebe.dto.business.admin.contractMng.ContractDTO;
import com.motorbikebe.dto.business.admin.contractMng.ContractSearchDTO;
import com.motorbikebe.entity.domain.ContractEntity;
import com.motorbikebe.repository.projection.ContractRevenueProjection;
import com.motorbikebe.repository.projection.DailyRevenueProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<ContractEntity, String> {

    /**
     * Tìm kiếm hợp đồng với nhiều điều kiện (query đã nâng cấp)
     * Note: Vì contract có nhiều xe, query này chỉ lấy thông tin cơ bản
     * Danh sách xe, phụ thu, thanh toán sẽ được load riêng
     */
    @Query(value = """
            SELECT con.id,
                   con.contract_code AS contractCode,
                   con.customer_id AS customerId,
                   cus.full_name AS customerName,
                   cus.phone_number AS phoneNumber,
                   cus.email,
                   con.source,
                   con.start_date AS startDate,
                   con.end_date AS endDate,
                   con.pickup_branch_id AS pickupBranchId,
                   pb.name AS pickupBranchName,
                   con.return_branch_id AS returnBranchId,
                   rb.name AS returnBranchName,
                   con.pickup_address AS pickupAddress,
                   con.return_address AS returnAddress,
                   con.total_rental_amount AS totalRentalAmount,
                   con.total_surcharge AS totalSurcharge,
                   con.discount_amount AS discountAmount,
                   con.final_amount AS finalAmount,
                   con.paid_amount AS paidAmount,
                   con.remaining_amount AS remainingAmount,
                   con.status,
                   con.notes,
                   con.delivery_time AS deliveryTime,
                   con.return_time AS returnTime,
                   con.completed_date AS completedDate
            FROM contract con
            INNER JOIN customer cus ON con.customer_id = cus.id
            LEFT JOIN branch pb ON con.pickup_branch_id = pb.id
            LEFT JOIN branch rb ON con.return_branch_id = rb.id
            WHERE (:#{#req.keyword} IS NULL OR :#{#req.keyword} = ''
                   OR con.contract_code LIKE %:#{#req.keyword}%
                   OR cus.full_name LIKE %:#{#req.keyword}%
                   OR cus.phone_number LIKE %:#{#req.keyword}%
                   OR EXISTS (
                       SELECT 1 FROM contract_car cc
                       INNER JOIN car c ON cc.car_id = c.id
                       WHERE cc.contract_id = con.id
                       AND c.license_plate LIKE %:#{#req.keyword}%
                   ))
            AND (:#{#req.status?.name()} IS NULL OR con.status = :#{#req.status?.name()})
            AND (:#{#req.source} IS NULL OR :#{#req.source} = '' OR con.source = :#{#req.source})
            AND (:#{#req.startDateFrom} IS NULL OR con.start_date >= :#{#req.startDateFrom})
            AND (:#{#req.startDateTo} IS NULL OR con.start_date <= :#{#req.startDateTo})
            AND (:#{#req.pickupBranchId} IS NULL OR :#{#req.pickupBranchId} = '' OR con.pickup_branch_id = :#{#req.pickupBranchId})
            AND (:#{#req.returnBranchId} IS NULL OR :#{#req.returnBranchId} = '' OR con.return_branch_id = :#{#req.returnBranchId})
            ORDER BY con.created_date DESC
            """, countQuery = """
            SELECT COUNT(con.id)
            FROM contract con
            INNER JOIN customer cus ON con.customer_id = cus.id
            WHERE (:#{#req.keyword} IS NULL OR :#{#req.keyword} = ''
                   OR con.contract_code LIKE %:#{#req.keyword}%
                   OR cus.full_name LIKE %:#{#req.keyword}%
                   OR cus.phone_number LIKE %:#{#req.keyword}%
                   OR EXISTS (
                       SELECT 1 FROM contract_car cc
                       INNER JOIN car c ON cc.car_id = c.id
                       WHERE cc.contract_id = con.id
                       AND c.license_plate LIKE %:#{#req.keyword}%
                   ))
            AND (:#{#req.status?.name()} IS NULL OR con.status = :#{#req.status?.name()})
            AND (:#{#req.source} IS NULL OR :#{#req.source} = '' OR con.source = :#{#req.source})
            AND (:#{#req.startDateFrom} IS NULL OR con.start_date >= :#{#req.startDateFrom})
            AND (:#{#req.startDateTo} IS NULL OR con.start_date <= :#{#req.startDateTo})
            AND (:#{#req.pickupBranchId} IS NULL OR :#{#req.pickupBranchId} = '' OR con.pickup_branch_id = :#{#req.pickupBranchId})
            AND (:#{#req.returnBranchId} IS NULL OR :#{#req.returnBranchId} = '' OR con.return_branch_id = :#{#req.returnBranchId})
            """, nativeQuery = true)
    Page<ContractDTO> searchContracts(Pageable pageable, @Param("req") ContractSearchDTO req);

    /**
     * Tìm hợp đồng theo khách hàng
     */
    List<ContractEntity> findByCustomerId(String customerId);

    /**
     * Tìm hợp đồng theo trạng thái
     */
    List<ContractEntity> findByStatus(ContractStatus status);

    /**
     * Tìm hợp đồng có chứa xe cụ thể và trong khoảng thời gian
     * (để kiểm tra xe có available không)
     */
    @Query(value = """
            SELECT con.* FROM contract con
            INNER JOIN contract_car cc ON con.id = cc.contract_id
            WHERE cc.car_id = :carId
            AND con.status IN ('CONFIRMED', 'DELIVERED')
            AND con.start_date <= :endDate
            AND con.end_date >= :startDate
            """, nativeQuery = true)
    List<ContractEntity> findOverlappingContracts(@Param("carId") String carId,
                                                    @Param("startDate") Date startDate,
                                                    @Param("endDate") Date endDate);

    /**
     * Generate mã hợp đồng mới (HDxxxx)
     */
    @Query(value = "SELECT CONCAT('HD', LPAD(IFNULL(MAX(CAST(SUBSTRING(contract_code, 3) AS UNSIGNED)), 0) + 1, 6, '0')) FROM contract WHERE contract_code LIKE 'HD%'", nativeQuery = true)
    String generateContractCode();

    @Query(value = """
            SELECT COUNT(con.id)
            FROM contract con
            WHERE con.status <> 'CANCELLED'
              AND (:branchId IS NULL OR :branchId = '' OR con.pickup_branch_id = :branchId OR con.return_branch_id = :branchId)
              AND con.start_date >= :startDate
              AND con.start_date < :endDate
            """, nativeQuery = true)
    long countContractsByBranchAndDate(@Param("branchId") String branchId,
                                       @Param("startDate") Date startDate,
                                       @Param("endDate") Date endDate);

    @Query(value = """
            SELECT 
                COALESCE(SUM(con.final_amount), 0) AS contractAmount,
                COALESCE(SUM(con.total_rental_amount), 0) AS rentalAmount,
                COALESCE(SUM(con.total_surcharge), 0) AS surchargeAmount
            FROM contract con
            WHERE con.status <> 'CANCELLED'
              AND (:branchId IS NULL OR :branchId = '' OR con.pickup_branch_id = :branchId OR con.return_branch_id = :branchId)
              AND con.start_date >= :startDate
              AND con.start_date < :endDate
            """, nativeQuery = true)
    ContractRevenueProjection sumRevenueByBranchAndDate(@Param("branchId") String branchId,
                                                         @Param("startDate") Date startDate,
                                                         @Param("endDate") Date endDate);

    @Query(value = """
            SELECT 
                DATE(con.start_date) AS revenueDate,
                COALESCE(SUM(con.final_amount), 0) AS contractAmount,
                COALESCE(SUM(con.total_rental_amount), 0) AS rentalAmount,
                COALESCE(SUM(con.total_surcharge), 0) AS surchargeAmount
            FROM contract con
            WHERE con.status <> 'CANCELLED'
              AND (:branchId IS NULL OR :branchId = '' OR con.pickup_branch_id = :branchId OR con.return_branch_id = :branchId)
              AND con.start_date >= :startDate
              AND con.start_date < :endDate
            GROUP BY DATE(con.start_date)
            ORDER BY revenueDate
            """, nativeQuery = true)
    List<DailyRevenueProjection> sumDailyRevenueByBranchAndDate(@Param("branchId") String branchId,
                                                                @Param("startDate") Date startDate,
                                                                @Param("endDate") Date endDate);
}


