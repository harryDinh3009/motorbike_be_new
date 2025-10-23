package com.translateai.repository.system;

import com.translateai.entity.system.RoleEntity;
import com.translateai.entity.system.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    UserRoleEntity findByUserId(String userId);

    @Query("""
            SELECT r FROM UserEntity u JOIN UserRoleEntity ur ON u.id = ur.userId
            JOIN RoleEntity r ON r.rlId = ur.rlId
            WHERE u.id = :userId
            """)
    List<RoleEntity> findByUser(@Param("userId") String userId);

}
