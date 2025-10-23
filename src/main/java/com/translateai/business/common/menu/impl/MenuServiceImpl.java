package com.translateai.business.common.menu.impl;

import com.translateai.business.common.menu.response.MenuModel;
import com.translateai.business.common.menu.response.MenuResponse;
import com.translateai.business.common.menu.service.MenuService;
import com.translateai.business.common.service.service.CommonService;
import com.translateai.entity.system.MenuEntity;
import com.translateai.repository.system.MenuRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author HoangDV
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuServiceImpl implements MenuService {

    MenuRepository menuRepository;

    CommonService commonService;

    @Override
    public List<MenuResponse> getMenuByCurrentRole() {
        // TODO:Get all menu by role
        var listMenuFindByCurrentRole = menuRepository.findMenuByCurrentRole(commonService.getUserCurrentInfo().getId())
                .stream()
                .sorted(Comparator.comparing(MenuEntity::getDisplayOrder))
                .toList();
        // TODO: Get menu parent
        var listMenuParent = listMenuFindByCurrentRole.stream().filter(menu -> menu.getParentId() == null).toList();
        // TODO: Convert to response json (name, path)
        var listMenuParentResult = listMenuParent.stream()
                .map(menu -> MenuResponse.builder()
                        .id(menu.getId())
                        .name(menu.getName())
                        .path(menu.getRoute())
                        .build()
                )
                .toList();
        // TODO: Get menu child
        listMenuParentResult.forEach(menu -> {
            menu.setListMenu(listMenuFindByCurrentRole.stream().filter(m -> Objects.nonNull(m.getParentId()) && m.getParentId().equals(menu.getId()))
                    .map(m -> MenuModel.builder().name(m.getName()).path(m.getRoute()).id(m.getId() + "").build()).toList());
        });
        return listMenuParentResult;
    }
}
