package com.translateai.business.admin.carMng.web;

import com.translateai.business.admin.carMng.service.CarMngService;
import com.translateai.common.ApiResponse;
import com.translateai.common.ApiStatus;
import com.translateai.common.PageableObject;
import com.translateai.constant.classconstant.CarConstants;
import com.translateai.constant.enumconstant.CarStatus;
import com.translateai.dto.business.admin.carMng.CarDTO;
import com.translateai.dto.business.admin.carMng.CarSaveDTO;
import com.translateai.dto.business.admin.carMng.CarSearchDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/a/car-mng")
@RequiredArgsConstructor
public class CarMngController {

    private final CarMngService carMngService;

    /**
     * Tìm kiếm xe với phân trang
     *
     * @param searchDTO DTO tìm kiếm
     * @return PageableObject<CarDTO>
     */
    @PostMapping("/list")
    public ApiResponse<PageableObject<CarDTO>> searchCars(@RequestBody CarSearchDTO searchDTO) {
        PageableObject<CarDTO> pageableRes = carMngService.searchCars(searchDTO);
        return new ApiResponse<>(ApiStatus.SUCCESS, pageableRes);
    }

    /**
     * Lấy chi tiết xe
     *
     * @param id ID xe
     * @return CarDTO
     */
    @GetMapping("/detail")
    public ApiResponse<CarDTO> getCarDetail(@RequestParam("id") String id) {
        CarDTO response = carMngService.getCarDetail(id);
        return new ApiResponse<>(ApiStatus.SUCCESS, response);
    }

    /**
     * Tạo mới hoặc cập nhật xe
     *
     * @param saveDTO DTO lưu xe
     * @return Boolean
     */
    @PostMapping("/save")
    public ApiResponse<Boolean> saveCar(@RequestBody CarSaveDTO saveDTO) {
        Boolean response = carMngService.saveCar(saveDTO);
        return new ApiResponse<>(ApiStatus.CREATED, response);
    }

    /**
     * Xóa xe
     *
     * @param id ID xe
     * @return Boolean
     */
    @DeleteMapping("/delete")
    public ApiResponse<Boolean> deleteCar(@RequestParam("id") String id) {
        Boolean response = carMngService.deleteCar(id);
        return new ApiResponse<>(ApiStatus.SUCCESS, response);
    }

    /**
     * Lấy tất cả xe
     *
     * @return List<CarDTO>
     */
    @GetMapping("/all")
    public ApiResponse<List<CarDTO>> getAllCars() {
        List<CarDTO> response = carMngService.getAllCars();
        return new ApiResponse<>(ApiStatus.SUCCESS, response);
    }

    /**
     * Upload ảnh xe
     *
     * @param carId ID xe
     * @param file File ảnh
     * @return URL ảnh
     */
    @PostMapping(value = "/upload-image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<String> uploadCarImage(
            @RequestParam("carId") String carId,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = carMngService.uploadCarImage(carId, file);
        return new ApiResponse<>(ApiStatus.SUCCESS, imageUrl);
    }

    /**
     * Lấy danh sách mẫu xe
     *
     * @return List<String>
     */
    @GetMapping("/car-models")
    public ApiResponse<List<String>> getCarModels() {
        return new ApiResponse<>(ApiStatus.SUCCESS, CarConstants.CAR_MODELS);
    }

    /**
     * Lấy danh sách loại xe
     *
     * @return List<String>
     */
    @GetMapping("/car-types")
    public ApiResponse<List<String>> getCarTypes() {
        return new ApiResponse<>(ApiStatus.SUCCESS, CarConstants.CAR_TYPES);
    }

    /**
     * Lấy danh sách tình trạng xe
     *
     * @return List<String>
     */
    @GetMapping("/car-conditions")
    public ApiResponse<List<String>> getCarConditions() {
        return new ApiResponse<>(ApiStatus.SUCCESS, CarConstants.CAR_CONDITIONS);
    }

    /**
     * Lấy danh sách màu sắc xe
     *
     * @return List<String>
     */
    @GetMapping("/car-colors")
    public ApiResponse<List<String>> getCarColors() {
        return new ApiResponse<>(ApiStatus.SUCCESS, CarConstants.CAR_COLORS);
    }

    /**
     * Lấy danh sách trạng thái xe
     *
     * @return List<Map<String, String>>
     */
    @GetMapping("/car-statuses")
    public ApiResponse<List<Map<String, String>>> getCarStatuses() {
        List<Map<String, String>> statuses = Arrays.stream(CarStatus.values())
                .map(status -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("code", status.name());
                    map.put("name", status.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
        return new ApiResponse<>(ApiStatus.SUCCESS, statuses);
    }
}

