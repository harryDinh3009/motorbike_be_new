package com.translateai.business.admin.carMng.service;

import com.translateai.common.PageableObject;
import com.translateai.dto.business.admin.carMng.CarDTO;
import com.translateai.dto.business.admin.carMng.CarSaveDTO;
import com.translateai.dto.business.admin.carMng.CarSearchDTO;
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
}

