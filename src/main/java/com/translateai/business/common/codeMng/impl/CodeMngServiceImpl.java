package com.translateai.business.common.codeMng.impl;

import com.translateai.business.common.codeMng.service.CodeMngService;
import com.translateai.dto.common.codeMng.CodeMngListDTO;
import com.translateai.dto.common.codeMng.CodeMngListReqDTO;
import com.translateai.dto.common.codeMng.CodeMngReqDTO;
import com.translateai.entity.common.codeMng.CodeMngEntity;
import com.translateai.repository.common.codeMng.CodeMngRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeMngServiceImpl implements CodeMngService {

    private final CodeMngRepository codeMngRepository;
    private final ModelMapper modelMapper;

    public CodeMngServiceImpl(ModelMapper modelMapper, CodeMngRepository codeMngRepository) {
        this.modelMapper = modelMapper;
        this.codeMngRepository = codeMngRepository;
        setupModelMapper();
    }

    private void setupModelMapper() {
        modelMapper.typeMap(CodeMngEntity.class, CodeMngListDTO.class).addMappings(mapper -> {
            mapper.map(CodeMngEntity::getCdId, CodeMngListDTO::setValue);
            mapper.map(CodeMngEntity::getCdNm, CodeMngListDTO::setLabel);
        });
    }

    private CodeMngListDTO mapToDto(CodeMngEntity entity) {
        return modelMapper.map(entity, CodeMngListDTO.class);
    }

    @Override
    public List<CodeMngListDTO> getListCodeMngByUpCdId(CodeMngReqDTO codeMngResDTO) {
        return codeMngRepository.selectByUpCdId(codeMngResDTO.getUpCdId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeMngListDTO> getListCodeMngByListUpCd(CodeMngListReqDTO codeMngResDTO) {
        return codeMngRepository.findByUpCdIdIn(codeMngResDTO.getUpCdIdList())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}