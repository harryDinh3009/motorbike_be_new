package com.motorbikebe.business.admin.carMng.service;

import com.motorbikebe.dto.business.admin.carMng.CarModelDTO;
import com.motorbikebe.dto.business.admin.carMng.CarModelSaveDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface CarModelService {

    List<CarModelDTO> getAllCarModels();

    List<String> getActiveModelNames();

    CarModelDTO createCarModel(@Valid CarModelSaveDTO saveDTO);

    CarModelDTO updateCarModel(String id, @Valid CarModelSaveDTO saveDTO);

    Boolean deleteCarModel(String id);
}

