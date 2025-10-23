package com.translateai.repository.system;

import com.translateai.entity.system.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {

    @Query(value = """
            SELECT 
            sr.url AS url, 
            COALESCE(GROUP_CONCAT(srr.rl_id ORDER BY srr.rl_id SEPARATOR ','), 'permitAll') AS roles
            FROM sys_resource_role srr JOIN 
            sys_resource sr ON sr.rs_id = srr.rs_id 
            GROUP BY srr.rs_id 
            """, nativeQuery = true)
    List<Map<String, Object>> findAllResources();

}
