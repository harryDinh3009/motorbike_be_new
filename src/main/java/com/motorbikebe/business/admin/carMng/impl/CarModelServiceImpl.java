package com.motorbikebe.business.admin.carMng.impl;

import com.motorbikebe.business.admin.carMng.service.CarModelService;
import com.motorbikebe.common.ApiStatus;
import com.motorbikebe.config.exception.RestApiException;
import com.motorbikebe.dto.business.admin.carMng.CarModelDTO;
import com.motorbikebe.dto.business.admin.carMng.CarModelSaveDTO;
import com.motorbikebe.entity.domain.CarModelEntity;
import com.motorbikebe.repository.business.admin.CarModelRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class CarModelServiceImpl implements CarModelService {

    private final CarModelRepository carModelRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CarModelDTO> getAllCarModels() {
        return carModelRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(entity -> modelMapper.map(entity, CarModelDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getActiveModelNames() {
        return carModelRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(CarModelEntity::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CarModelDTO createCarModel(@Valid CarModelSaveDTO saveDTO) {
        validateNameUnique(saveDTO.getName(), null);

        CarModelEntity entity = CarModelEntity.builder()
                .name(saveDTO.getName().trim())
                .brand(StringUtils.trimToNull(saveDTO.getBrand()))
                .description(StringUtils.trimToNull(saveDTO.getDescription()))
                .active(saveDTO.getActive() != null ? saveDTO.getActive() : Boolean.TRUE)
                .build();

        CarModelEntity saved = carModelRepository.save(entity);
        return modelMapper.map(saved, CarModelDTO.class);
    }

    @Override
    @Transactional
    public CarModelDTO updateCarModel(String id, @Valid CarModelSaveDTO saveDTO) {
        CarModelEntity entity = carModelRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));

        if (StringUtils.isNotBlank(saveDTO.getName())) {
            validateNameUnique(saveDTO.getName(), id);
            entity.setName(saveDTO.getName().trim());
        }

        if (saveDTO.getBrand() != null) {
            entity.setBrand(StringUtils.trimToNull(saveDTO.getBrand()));
        }
        if (saveDTO.getDescription() != null) {
            entity.setDescription(StringUtils.trimToNull(saveDTO.getDescription()));
        }
        if (saveDTO.getActive() != null) {
            entity.setActive(saveDTO.getActive());
        }

        CarModelEntity saved = carModelRepository.save(entity);
        return modelMapper.map(saved, CarModelDTO.class);
    }

    @Override
    @Transactional
    public Boolean deleteCarModel(String id) {
        CarModelEntity entity = carModelRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));
        carModelRepository.delete(entity);
        return true;
    }

    private void validateNameUnique(String name, String excludeId) {
        if (StringUtils.isBlank(name)) {
            throw new RestApiException(ApiStatus.BAD_REQUEST);
        }
        String normalized = name.trim();
        boolean exists = excludeId == null
                ? carModelRepository.existsByNameIgnoreCase(normalized)
                : carModelRepository.existsByNameIgnoreCaseAndIdNot(normalized, excludeId);
        if (exists) {
            throw new RestApiException(ApiStatus.CONFLICT);
        }
    }
}

