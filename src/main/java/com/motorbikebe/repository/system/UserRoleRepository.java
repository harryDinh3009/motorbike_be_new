package com.motorbikebe.repository.system;

import com.motorbikebe.entity.system.RoleEntity;
import com.motorbikebe.entity.system.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    UserRoleEntity findByUserId(String userId);

    List<UserRoleEntity> findAllByUserId(String userId);

    @Query("""
            SELECT r FROM UserEntity u JOIN UserRoleEntity ur ON u.id = ur.userId
            JOIN RoleEntity r ON r.rlId = ur.rlId
            WHERE u.id = :userId
            """)
    List<RoleEntity> findByUser(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sys_user_role WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserId(@Param("userId") String userId);

}
