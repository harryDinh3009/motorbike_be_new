package com.motorbikebe.business.admin.carMng.service;

import com.motorbikebe.common.PageableObject;
import com.motorbikebe.dto.business.admin.carMng.CarDTO;
import com.motorbikebe.dto.business.admin.carMng.CarSaveDTO;
import com.motorbikebe.dto.business.admin.carMng.CarSearchDTO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CarMngService {

    /**
     * Tìm kiếm xe với phân trang
     *
     * @param searchDTO DTO tìm kiếm
     * @return PageableObject<CarDTO>
     */
    PageableObject<CarDTO> searchCars(CarSearchDTO searchDTO);

    /**
     * Lấy chi tiết xe
     *
     * @param id ID xe
     * @return CarDTO
     */
    CarDTO getCarDetail(String id);

    /**
     * Tạo mới hoặc cập nhật xe
     *
     * @param saveDTO DTO lưu xe
     * @return Boolean
     */
    Boolean saveCar(@Valid CarSaveDTO saveDTO);

    /**
     * Xóa xe
     *
     * @param id ID xe
     * @return Boolean
     */
    Boolean deleteCar(String id);

    /**
     * Lấy tất cả xe
     *
     * @return List<CarDTO>
     */
    List<CarDTO> getAllCars();

    /**
     * Upload ảnh xe
     *
     * @param carId ID xe
     * @param file File ảnh
     * @return URL ảnh
     */
    String uploadCarImage(String carId, MultipartFile file);

    /**
     * Tìm kiếm xe có sẵn với phân trang
     * - Chỉ lấy các xe thuộc chi nhánh của người đang đăng nhập
     * - Nếu truyền startDate và endDate: kiểm tra xe có trong hợp đồng nào có thời gian trùng lặp không
     *   + Nếu trùng: chuyển status về NOT_AVAILABLE
     *   + Nếu không trùng: giữ nguyên status của xe
     *
     * @param searchDTO DTO tìm kiếm (bao gồm startDate và endDate)
     * @return PageableObject<CarDTO>
     */
    PageableObject<CarDTO> searchAvailableCars(CarSearchDTO searchDTO);
}

