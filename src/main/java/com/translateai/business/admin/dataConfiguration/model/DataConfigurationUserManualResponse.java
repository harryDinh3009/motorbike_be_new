package com.translateai.business.admin.dataConfiguration.model;

/**
 * @author HoangDV
 */
public interface DataConfigurationUserManualResponse {
    Integer getRowNum();

    String getId();

    String getName();

    String getCode();

    String getDescription();

    Integer getStatus();

    Long getCreatedDate();
}
