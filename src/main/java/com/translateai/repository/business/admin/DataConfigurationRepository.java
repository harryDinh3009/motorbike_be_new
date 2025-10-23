package com.translateai.repository.business.admin;

import com.translateai.business.admin.dataConfiguration.model.DataConfigurationListResponse;
import com.translateai.business.admin.dataConfiguration.model.DataConfigurationUserManualResponse;
import com.translateai.entity.domain.DataConfigurationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataConfigurationRepository extends JpaRepository<DataConfigurationEntity, String> {

    @Query(value = """
            SELECT ROW_NUMBER() OVER (ORDER BY dc.created_date DESC) AS rowNum,
                   dc.id, dc.name, dc.description,
                   upDc.name AS nameUp, sr.rl_nm AS roleNm, dc.created_date AS createdDate
            FROM data_configuration dc
            LEFT JOIN data_configuration upDc ON dc.up_id = upDc.id
            LEFT JOIN sys_role sr ON sr.rl_cd = dc.role
            WHERE dc.category = :category
            ORDER BY dc.created_date DESC
            """, nativeQuery = true)
    Page<DataConfigurationListResponse> getPageDataConfiguration(Pageable pageable,
                                                                 @Param("category") String category);

    @Query(value = """
            SELECT ROW_NUMBER() over (ORDER BY created_date DESC ) AS rowNum,
                   id,
                   name,
                   code,
                   description,
                   status,
                   created_date AS createdDate
            FROM data_configuration
            WHERE category = :category
              AND (:name IS NULL OR name LIKE :name)
              AND (:status IS NULL OR status = :status)
            ORDER BY created_date DESC
            """, nativeQuery = true)
    Page<DataConfigurationUserManualResponse> getPageDataConfigurationUserManual(Pageable pageable,
                                                                                 @Param("category") String category,
                                                                                 @Param("name") String name,
                                                                                 @Param("status") Integer status);

    @Query(value = """
            SELECT ROW_NUMBER() OVER (ORDER BY dc.created_date DESC) AS rowNum,
                   dc.id,
                   dc.name,
                   dc.description,
                   upDc.name                                         AS nameUp,
                   upDc.id                                           as idUp,
                   sr.rl_nm                                          AS roleNm,
                   dc.created_date                                   AS createdDate
            FROM data_configuration dc
                     LEFT JOIN data_configuration upDc ON dc.up_id = upDc.id
                     LEFT JOIN sys_role sr ON sr.rl_cd = dc.role
            WHERE dc.category = :category
            ORDER BY dc.created_date DESC
            """, nativeQuery = true)
    Page<DataConfigurationListResponse> getPageDataConfigurationByJob(Pageable pageable,
                                                                      @Param("category") String category);

    List<DataConfigurationEntity> findDataConfigurationEntitiesByCategoryIn(List<String> categories);

    List<DataConfigurationEntity> findAllByUpId(String upId);

    List<DataConfigurationEntity> findAllByCategory(String category);

}
