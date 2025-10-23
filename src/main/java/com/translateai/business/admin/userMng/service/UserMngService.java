package com.translateai.business.admin.userMng.service;

import com.translateai.common.PageableObject;
import com.translateai.dto.business.admin.userMng.UserMngListDTO;
import com.translateai.dto.business.admin.userMng.UserMngSaveDTO;
import com.translateai.dto.business.admin.userMng.UserMngSearchDTO;
import jakarta.validation.Valid;

public interface UserMngService {

    /**
     * Get Page User
     *
     * @param userMngSearchDTO .
     * @return PageableObject<UserMngListDTO>
     */
    PageableObject<UserMngListDTO> getPageUser(UserMngSearchDTO userMngSearchDTO);

    /**
     * Save User
     *
     * @param userMngSaveDTO .
     * @return Boolean
     */
    Boolean saveUser(@Valid UserMngSaveDTO userMngSaveDTO);

    /**
     * Detail User
     *
     * @param id .
     * @return UserMngSaveDTO
     */
    UserMngSaveDTO detailUser(String id);

}
