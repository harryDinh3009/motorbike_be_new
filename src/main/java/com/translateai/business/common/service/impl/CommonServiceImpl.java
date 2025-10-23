package com.translateai.business.common.service.impl;

import com.translateai.business.common.service.service.CommonService;
import com.translateai.config.security.custom.UserDetailsImpl;
import com.translateai.dto.common.userCurrent.UserCurrentInfoDTO;
import com.translateai.entity.domain.UserEntity;
import com.translateai.repository.business.admin.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonServiceImpl implements CommonService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Override
    public UserCurrentInfoDTO getUserCurrentInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                return null;
            }
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            String userCurrentId = userDetails.getId();
            UserEntity userEntity = userRepository.findById(userCurrentId).orElse(null);
            UserCurrentInfoDTO userCurrentInfoDTO = modelMapper.map(userEntity, UserCurrentInfoDTO.class);

            return userCurrentInfoDTO;
        }
        return null;
    }

}
