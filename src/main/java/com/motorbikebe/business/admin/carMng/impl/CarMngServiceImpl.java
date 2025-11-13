package com.motorbikebe.business.admin.carMng.impl;

import com.motorbikebe.business.admin.carMng.service.CarMngService;
import com.motorbikebe.business.common.service.service.CommonService;
import com.motorbikebe.common.ApiStatus;
import com.motorbikebe.common.PageableObject;
import com.motorbikebe.config.cloudinary.CloudinaryUploadImages;
import com.motorbikebe.config.exception.RestApiException;
import com.motorbikebe.dto.business.admin.carMng.CarDTO;
import com.motorbikebe.dto.business.admin.carMng.CarSaveDTO;
import com.motorbikebe.dto.business.admin.carMng.CarSearchDTO;
import com.motorbikebe.dto.common.userCurrent.UserCurrentInfoDTO;
import com.motorbikebe.entity.domain.CarEntity;
import com.motorbikebe.repository.business.admin.CarRepository;
import com.motorbikebe.util.CloudinaryUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class CarMngServiceImpl implements CarMngService {

    private final CarRepository carRepository;
    private final CloudinaryUploadImages cloudinaryUploadImages;
    private final ModelMapper modelMapper;
    private final CommonService commonService;

    @Override
    public PageableObject<CarDTO> searchCars(CarSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<CarDTO> carPage = carRepository.searchCars(pageable, searchDTO);
        
        // Set status name
        carPage.getContent().forEach(car -> {
            if (car.getStatus() != null) {
                car.setStatusNm(car.getStatus().getDescription());
            }
        });
        
        return new PageableObject<>(carPage);
    }

    @Override
    public CarDTO getCarDetail(String id) {
        Optional<CarEntity> carEntity = carRepository.findById(id);
        if (!carEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        CarDTO carDTO = modelMapper.map(carEntity.get(), CarDTO.class);
        if (carDTO.getStatus() != null) {
            carDTO.setStatusNm(carDTO.getStatus().getDescription());
        }
        
        return carDTO;
    }

    @Override
    @Transactional
    public Boolean saveCar(@Valid CarSaveDTO saveDTO) {
        CarEntity carEntity;
        boolean isNew = StringUtils.isBlank(saveDTO.getId());

        if (isNew) {
            carEntity = new CarEntity();
        } else {
            Optional<CarEntity> carEntityFind = carRepository.findById(saveDTO.getId());
            if (!carEntityFind.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            carEntity = carEntityFind.get();
        }

        carEntity.setModel(saveDTO.getModel());
        carEntity.setLicensePlate(saveDTO.getLicensePlate());
        carEntity.setCarType(saveDTO.getCarType());
        carEntity.setBranchId(saveDTO.getBranchId());
        carEntity.setDailyPrice(saveDTO.getDailyPrice());
        carEntity.setHourlyPrice(saveDTO.getHourlyPrice());
        carEntity.setCondition(saveDTO.getCondition());
        carEntity.setCurrentOdometer(saveDTO.getCurrentOdometer());
        carEntity.setStatus(saveDTO.getStatus());
        carEntity.setImageUrl(saveDTO.getImageUrl());
        carEntity.setNote(saveDTO.getNote());
        
        // Thông tin bổ sung
        carEntity.setYearOfManufacture(saveDTO.getYearOfManufacture());
        carEntity.setOrigin(saveDTO.getOrigin());
        carEntity.setValue(saveDTO.getValue());
        carEntity.setFrameNumber(saveDTO.getFrameNumber());
        carEntity.setEngineNumber(saveDTO.getEngineNumber());
        carEntity.setColor(saveDTO.getColor());
        carEntity.setRegistrationNumber(saveDTO.getRegistrationNumber());
        carEntity.setRegisteredOwnerName(saveDTO.getRegisteredOwnerName());
        carEntity.setRegistrationPlace(saveDTO.getRegistrationPlace());
        carEntity.setInsuranceContractNumber(saveDTO.getInsuranceContractNumber());
        carEntity.setInsuranceExpiryDate(saveDTO.getInsuranceExpiryDate());

        carRepository.save(carEntity);
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteCar(String id) {
        Optional<CarEntity> carEntity = carRepository.findById(id);
        if (!carEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        carRepository.deleteById(id);
        return true;
    }

    @Override
    public List<CarDTO> getAllCars() {
        List<CarEntity> carEntities = carRepository.findAll();
        return carEntities.stream()
                .map(entity -> {
                    CarDTO dto = modelMapper.map(entity, CarDTO.class);
                    if (dto.getStatus() != null) {
                        dto.setStatusNm(dto.getStatus().getDescription());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String uploadCarImage(String carId, MultipartFile file) {
        // Tìm xe
        Optional<CarEntity> carEntity = carRepository.findById(carId);
        if (!carEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        // Xóa ảnh cũ nếu có
        if (StringUtils.isNotBlank(carEntity.get().getImageUrl())) {
            try {
                String publicId = CloudinaryUtils.extractPublicId(carEntity.get().getImageUrl());
                cloudinaryUploadImages.deleteImage(publicId);
            } catch (Exception e) {
                // Log error but continue with upload
            }
        }

        // Upload ảnh mới
        String imageUrl = cloudinaryUploadImages.uploadImage(file, "car-images");
        
        // Cập nhật URL vào database
        carEntity.get().setImageUrl(imageUrl);
        carRepository.save(carEntity.get());

        return imageUrl;
    }

    @Override
    public PageableObject<CarDTO> searchAvailableCars(CarSearchDTO searchDTO) {
        // Lấy thông tin user hiện tại
        UserCurrentInfoDTO userCurrentInfo = commonService.getUserCurrentInfo();
        if (userCurrentInfo != null && StringUtils.isNotBlank(userCurrentInfo.getBranchId())) {
            // Set branchId của user hiện tại vào searchDTO để lọc xe theo chi nhánh
            searchDTO.setBranchId(userCurrentInfo.getBranchId());
        }

        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<CarDTO> carPage = carRepository.searchAvailableCars(pageable, searchDTO);

        // Set status name
        carPage.getContent().forEach(car -> {
            if (car.getStatus() != null) {
                car.setStatusNm(car.getStatus().getDescription());
            }
        });

        return new PageableObject<>(carPage);
    }
}

