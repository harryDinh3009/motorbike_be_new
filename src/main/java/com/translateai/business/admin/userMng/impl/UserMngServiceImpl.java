package com.translateai.business.admin.userMng.impl;

import com.translateai.business.admin.userMng.response.UserMngListResponse;
import com.translateai.business.admin.userMng.service.UserMngService;
import com.translateai.business.common.service.service.CommonService;
import com.translateai.common.ApiStatus;
import com.translateai.common.Constants;
import com.translateai.common.PageableObject;
import com.translateai.config.exception.RestApiException;
import com.translateai.config.mail.EmailSender;
import com.translateai.constant.classconstant.ScreenConstants;
import com.translateai.dto.business.admin.userMng.UserMngListDTO;
import com.translateai.dto.business.admin.userMng.UserMngSaveDTO;
import com.translateai.dto.business.admin.userMng.UserMngSearchDTO;
import com.translateai.entity.domain.UserEntity;
import com.translateai.entity.system.RoleEntity;
import com.translateai.entity.system.UserRoleEntity;
import com.translateai.repository.business.admin.UserRepository;
import com.translateai.repository.system.RoleRepository;
import com.translateai.repository.system.UserRoleRepository;
import com.translateai.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class UserMngServiceImpl implements UserMngService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailSender emailSender;

    private final CommonService commonService;

    /**
     * Get Page User
     *
     * @param userMngSearchDTO .
     * @return PageableObject<UserMngListDTO>
     */
    @Override
    public PageableObject<UserMngListDTO> getPageUser(UserMngSearchDTO userMngSearchDTO) {
        List<String> listCdMentorMentee = new ArrayList<>();
        listCdMentorMentee.add(Constants.CD_ROLE_MENTOR);
        listCdMentorMentee.add(Constants.CD_ROLE_CLIENT);

        Pageable pageable = PageRequest.of(userMngSearchDTO.getPage() - 1, userMngSearchDTO.getSize());
        Page<UserMngListResponse> pageResponse = userRepository.getPageUserMng(pageable, userMngSearchDTO, listCdMentorMentee);

        Page<UserMngListDTO> dtoPage = pageResponse.map(
                response -> modelMapper.map(response, UserMngListDTO.class)
        );

        return new PageableObject<>(dtoPage);
    }

    /**
     * Save User
     *
     * @param userMngSaveDTO .
     * @return Boolean
     */
    @Override
    @Transactional
    public Boolean saveUser(@Valid UserMngSaveDTO userMngSaveDTO) {
        UserEntity userEntity = new UserEntity();
        boolean isNew = StringUtils.isBlank(userMngSaveDTO.getId());

        if (isNew) {

            UserEntity userFindUsername = userRepository.findByUserName(userMngSaveDTO.getUsername());
            if (Objects.nonNull(userFindUsername)) {
                throw new RestApiException(ApiStatus.BAD_REQUEST_USERNAME_EXISTS);
            }

            UserEntity userFindEmail = userRepository.findByEmail(userMngSaveDTO.getEmail());
            if (Objects.nonNull(userFindEmail)) {
                throw new RestApiException(ApiStatus.BAD_REQUEST_EMAIL_EXISTS);
            }

        } else {

            Optional<UserEntity> userEntityFind = userRepository.findById(userMngSaveDTO.getId());
            if (!userEntityFind.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }

            UserEntity userFindUsernameUpdate = userRepository.findByUserNameNotInId(userMngSaveDTO.getUsername(), userMngSaveDTO.getId());
            if (Objects.nonNull(userFindUsernameUpdate)) {
                throw new RestApiException(ApiStatus.BAD_REQUEST_USERNAME_EXISTS);
            }

            userEntity = userEntityFind.get();

        }

        userEntity.setUserName(userMngSaveDTO.getUsername());
        userEntity.setFullName(userMngSaveDTO.getFullName());
        userEntity.setEmail(userMngSaveDTO.getEmail());
        userEntity.setGender(userMngSaveDTO.getGenderCd());
        userEntity.setDateOfBirth(Utils.convertStringToDate(userMngSaveDTO.getDateOfBirth(), "yyyy-MM-dd"));
        userEntity.setPhoneNumber(userMngSaveDTO.getPhoneNumber());
        userEntity.setStatus(userMngSaveDTO.getStatusCd());
        userEntity.setAvatar(ScreenConstants.URL_AVATAR_DEFAULT);

        if (isNew) {
            String password = Utils.generateRandomPassword();
            userEntity.setPassword(passwordEncoder.encode(password));
            String htmlBody = "<p>Mật khẩu của bạn là:</p><br/><div style=\"text-align: center; font-weight: bold; font-size: 25px;\"><strong>" + password + "</strong></div>";
            emailSender.sendEmail(new String[]{userMngSaveDTO.getEmail()}, "[MENTOR_MATCH] Thông báo mật khẩu", "Thông báo mật khẩu sau khi đăng ký tài khoản", htmlBody);
        }

        UserEntity savedUser = userRepository.save(userEntity);

        if (isNew) {
            RoleEntity roleEntity = roleRepository.findByRlCd(userMngSaveDTO.getRoleCd());
            if (Objects.isNull(roleEntity)) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setUserId(savedUser.getId());
            userRoleEntity.setRlId(roleEntity.getRlId());
            userRoleRepository.save(userRoleEntity);
        }

        return true;
    }

    /**
     * Detail User
     *
     * @param id .
     * @return UserMngSaveDTO
     */
    @Override
    public UserMngSaveDTO detailUser(String id) {
        UserMngSaveDTO userMngSaveDTO = new UserMngSaveDTO();

        Optional<UserEntity> userEntityFind = userRepository.findById(id);
        if (!userEntityFind.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        userMngSaveDTO.setId(userEntityFind.get().getId());
        userMngSaveDTO.setUsername(userEntityFind.get().getUserName());
        userMngSaveDTO.setEmail(userEntityFind.get().getEmail());
        userMngSaveDTO.setFullName(userEntityFind.get().getFullName());
        userMngSaveDTO.setGenderCd(userEntityFind.get().getGender());
        userMngSaveDTO.setDateOfBirth(Utils.convertDateToString(userEntityFind.get().getDateOfBirth(), "yyyy-MM-dd"));
        userMngSaveDTO.setPhoneNumber(userEntityFind.get().getPhoneNumber());
        userMngSaveDTO.setStatusCd(userEntityFind.get().getStatus());

        UserRoleEntity userRoleEntity = userRoleRepository.findByUserId(userEntityFind.get().getId());
        if (Objects.isNull(userRoleEntity)) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        Optional<RoleEntity> roleEntityFind = roleRepository.findById(userRoleEntity.getRlId());
        if (!roleEntityFind.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        userMngSaveDTO.setRoleCd(roleEntityFind.get().getRlCd());

        return userMngSaveDTO;
    }

}
