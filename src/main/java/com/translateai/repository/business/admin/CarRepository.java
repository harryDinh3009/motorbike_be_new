package com.translateai.repository.business.admin;

import com.translateai.constant.enumconstant.CarStatus;
import com.translateai.dto.business.admin.carMng.CarDTO;
import com.translateai.dto.business.admin.carMng.CarSearchDTO;
import com.translateai.entity.domain.CarEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<CarEntity, String> {

    @Query(value = """
            SELECT c.id,
                   c.model,
                   c.license_plate AS licensePlate,
                   c.car_type AS carType,
                   c.branch_id AS branchId,
                   b.name AS branchName,
                   c.daily_price AS dailyPrice,
                   c.hourly_price AS hourlyPrice,
                   c.condition,
                   c.current_odometer AS currentOdometer,
                   c.status,
                   c.image_url AS imageUrl,
                   c.note,
                   c.year_of_manufacture AS yearOfManufacture,
                   c.origin,
                   c.value,
                   c.frame_number AS frameNumber,
                   c.engine_number AS engineNumber,
                   c.color,
                   c.registration_number AS registrationNumber,
                   c.registered_owner_name AS registeredOwnerName,
                   c.registration_place AS registrationPlace,
                   c.insurance_contract_number AS insuranceContractNumber,
                   c.insurance_expiry_date AS insuranceExpiryDate
            FROM car c
            LEFT JOIN branch b ON c.branch_id = b.id
            WHERE (:#{#req.keyword} IS NULL OR :#{#req.keyword} = '' 
                   OR c.model LIKE %:#{#req.keyword}% 
                   OR c.license_plate LIKE %:#{#req.keyword}%)
            AND (:#{#req.branchId} IS NULL OR :#{#req.branchId} = '' OR c.branch_id = :#{#req.branchId})
            AND (:#{#req.carType} IS NULL OR :#{#req.carType} = '' OR c.car_type = :#{#req.carType})
            AND (:#{#req.condition} IS NULL OR :#{#req.condition} = '' OR c.condition = :#{#req.condition})
            AND (:#{#req.status} IS NULL OR c.status = :#{#req.status})
            ORDER BY c.created_date DESC
            """, countQuery = """
            SELECT COUNT(c.id)
            FROM car c
            LEFT JOIN branch b ON c.branch_id = b.id
            WHERE (:#{#req.keyword} IS NULL OR :#{#req.keyword} = '' 
                   OR c.model LIKE %:#{#req.keyword}% 
                   OR c.license_plate LIKE %:#{#req.keyword}%)
            AND (:#{#req.branchId} IS NULL OR :#{#req.branchId} = '' OR c.branch_id = :#{#req.branchId})
            AND (:#{#req.carType} IS NULL OR :#{#req.carType} = '' OR c.car_type = :#{#req.carType})
            AND (:#{#req.condition} IS NULL OR :#{#req.condition} = '' OR c.condition = :#{#req.condition})
            AND (:#{#req.status} IS NULL OR c.status = :#{#req.status})
            """, nativeQuery = true)
    Page<CarDTO> searchCars(Pageable pageable, @Param("req") CarSearchDTO req);

    CarEntity findByLicensePlate(String licensePlate);

    List<CarEntity> findByStatus(CarStatus status);
    
    List<CarEntity> findByBranchId(String branchId);
}

