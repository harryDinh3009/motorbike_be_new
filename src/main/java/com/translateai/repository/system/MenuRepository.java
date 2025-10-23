package com.translateai.repository.system;

import com.translateai.entity.system.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    @Query(value = """
            SELECT *
            FROM menu m
            WHERE m.id IN (SELECT menu_id
                           FROM menu_role mr
                           WHERE mr.role_id = (SELECT sr.rl_id
                                               FROM sys_user_role sur
                                                        JOIN sys_role sr
                                                             ON sr.rl_id = sur.rl_id
                                               WHERE sur.user_id = :userId))
            """, countQuery = """
            SELECT COUNT(m.id)
            FROM menu m
            WHERE m.id IN (SELECT menu_id
                           FROM menu_role mr
                           WHERE mr.role_id = (SELECT sr.rl_id
                                               FROM sys_user_role sur
                                                        JOIN sys_role sr
                                                             ON sr.rl_id = sur.rl_id
                                               WHERE sur.user_id = :userId))
            """, nativeQuery = true)
    List<MenuEntity> findMenuByCurrentRole(@Param("userId") String userId);
}
