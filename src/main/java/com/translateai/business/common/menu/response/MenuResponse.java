package com.translateai.business.common.menu.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author HoangDV
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MenuResponse {
    Long id;
    String name;
    String path;
    List<MenuModel> listMenu;
}
