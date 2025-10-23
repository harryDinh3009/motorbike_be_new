package com.translateai.business.common.menu.web;

import com.translateai.business.common.menu.response.MenuResponse;
import com.translateai.business.common.menu.service.MenuService;
import com.translateai.common.ApiResponse;
import com.translateai.common.ApiStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author HoangDV
 */
@RestController
@RequestMapping("/cmm/menu")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MenuController {

    MenuService menuService;

    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenu() {

        return new ApiResponse<>(ApiStatus.SUCCESS, menuService.getMenuByCurrentRole());
    }
}
