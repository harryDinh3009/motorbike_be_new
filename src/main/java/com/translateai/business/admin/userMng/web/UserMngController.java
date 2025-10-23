package com.translateai.business.admin.userMng.web;

import com.translateai.business.admin.userMng.service.UserMngService;
import com.translateai.common.ApiResponse;
import com.translateai.common.ApiStatus;
import com.translateai.common.PageableObject;
import com.translateai.dto.business.admin.userMng.UserMngListDTO;
import com.translateai.dto.business.admin.userMng.UserMngSaveDTO;
import com.translateai.dto.business.admin.userMng.UserMngSearchDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/a/user-mng")
@RequiredArgsConstructor
public class UserMngController {

    private final UserMngService userMngService;

    /**
     * Get Page User
     *
     * @param userMngSearchDTO .
     * @return PageableObject<UserMngListDTO>
     */
    @GetMapping("/list")
    public ApiResponse<PageableObject<UserMngListDTO>> getPageUser(UserMngSearchDTO userMngSearchDTO) {
        PageableObject<UserMngListDTO> pageableRes = userMngService.getPageUser(userMngSearchDTO);

        return new ApiResponse<>(ApiStatus.SUCCESS, pageableRes);
    }

    /**
     * Save User
     *
     * @param userMngSaveDTO .
     * @return Boolean
     */
    @PostMapping("/save")
    public ApiResponse<Boolean> saveUser(@RequestBody UserMngSaveDTO userMngSaveDTO) {
        Boolean response = userMngService.saveUser(userMngSaveDTO);

        return new ApiResponse<>(ApiStatus.CREATED, response);
    }

    /**
     * Detail User
     *
     * @param id .
     * @return UserMngSaveDTO
     */
    @GetMapping("/detail")
    public ApiResponse<UserMngSaveDTO> detailUser(@RequestParam("id") String id) {
        UserMngSaveDTO response = userMngService.detailUser(id);

        return new ApiResponse<>(ApiStatus.CREATED, response);
    }

}
