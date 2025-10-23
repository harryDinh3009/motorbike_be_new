package com.translateai.business.common.menu.service;

import com.translateai.business.common.menu.response.MenuResponse;

import java.util.List;

/**
 * @author HoangDV
 */
public interface MenuService {
    List<MenuResponse> getMenuByCurrentRole();
}
