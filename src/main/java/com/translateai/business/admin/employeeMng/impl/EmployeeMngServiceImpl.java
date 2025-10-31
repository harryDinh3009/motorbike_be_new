package com.translateai.business.admin.employeeMng.impl;

import com.translateai.business.admin.employeeMng.service.EmployeeMngService;
import com.translateai.common.ApiStatus;
import com.translateai.common.PageableObject;
import com.translateai.config.exception.RestApiException;
import com.translateai.dto.business.admin.employeeMng.EmployeeDTO;
import com.translateai.dto.business.admin.employeeMng.EmployeeSaveDTO;
import com.translateai.dto.business.admin.employeeMng.EmployeeSearchDTO;
import com.translateai.entity.domain.EmployeeEntity;
import com.translateai.repository.business.admin.EmployeeRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class EmployeeMngServiceImpl implements EmployeeMngService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @Override
    public PageableObject<EmployeeDTO> searchEmployees(EmployeeSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<EmployeeDTO> employeePage = employeeRepository.searchEmployees(pageable, searchDTO);
        return new PageableObject<>(employeePage);
    }

    @Override
    public EmployeeDTO getEmployeeDetail(String id) {
        Optional<EmployeeEntity> employeeEntity = employeeRepository.findById(id);
        if (!employeeEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        EmployeeEntity employee = employeeEntity.get();
        EmployeeDTO dto = modelMapper.map(employee, EmployeeDTO.class);
        
        return dto;
    }

    @Override
    @Transactional
    public Boolean saveEmployee(@Valid EmployeeSaveDTO saveDTO) {
        EmployeeEntity employeeEntity;
        boolean isNew = StringUtils.isBlank(saveDTO.getId());

        if (isNew) {
            employeeEntity = new EmployeeEntity();
        } else {
            Optional<EmployeeEntity> employeeEntityFind = employeeRepository.findById(saveDTO.getId());
            if (!employeeEntityFind.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            employeeEntity = employeeEntityFind.get();
        }

        employeeEntity.setFullName(saveDTO.getFullName());
        employeeEntity.setPhoneNumber(saveDTO.getPhoneNumber());
        employeeEntity.setEmail(saveDTO.getEmail());
        employeeEntity.setDateOfBirth(saveDTO.getDateOfBirth());
        employeeEntity.setGender(saveDTO.getGender());
        employeeEntity.setAddress(saveDTO.getAddress());
        employeeEntity.setBranchId(saveDTO.getBranchId());
        employeeEntity.setRole(saveDTO.getRole());
        employeeEntity.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : 1);

        employeeRepository.save(employeeEntity);
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteEmployee(String id) {
        Optional<EmployeeEntity> employeeEntity = employeeRepository.findById(id);
        if (!employeeEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        employeeRepository.deleteById(id);
        return true;
    }

    @Override
    public List<EmployeeDTO> getEmployeesByBranch(String branchId) {
        List<EmployeeEntity> employees = employeeRepository.findByBranchId(branchId);
        return employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
    }
}

