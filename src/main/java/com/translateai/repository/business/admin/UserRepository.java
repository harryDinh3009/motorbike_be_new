package com.translateai.repository.business.admin;

import com.translateai.business.admin.userMng.response.UserMngListResponse;
import com.translateai.dto.business.admin.userMng.UserMngSearchDTO;
import com.translateai.dto.common.user.UserProfileProjection;
import com.translateai.entity.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Query (value = """
            SELECT ROW_NUMBER() OVER (ORDER BY u.created_date DESC) AS rowNum,
            u.id,
            COALESCE(u.user_name, '-') AS userName,
            COALESCE(u.full_name, '-') AS fullName,
            COALESCE(u.email, '-') AS email,
            COALESCE(FN_GET_CODE_NAME(u.gender), '-') AS genderNm,
            COALESCE(sr.rl_nm, '-') AS roleNm,
            COALESCE(u.phone_number, '-') AS phoneNumber,
            COALESCE(FN_GET_CODE_NAME(u.status), '-') AS statusNm,
            u.avatar
            FROM user u JOIN sys_user_role sur ON u.id = sur.user_id
            JOIN sys_role sr ON sr.rl_id = sur.rl_id
            WHERE (:#{#req.fullName} IS NULL OR :#{#req.fullName} LIKE '' OR u.full_name LIKE %:#{#req.fullName}% OR u.user_name LIKE %:#{#req.fullName}%)
            AND (:#{#req.email} IS NULL OR :#{#req.email} LIKE '' OR u.email LIKE %:#{#req.email}%)
            AND (:#{#req.role} IS NULL OR :#{#req.role} LIKE '' OR sr.rl_cd = :#{#req.role})
            AND (:#{#req.gender} IS NULL OR :#{#req.gender} LIKE '' OR u.gender = :#{#req.gender})
            AND (:#{#req.phoneNumber} IS NULL OR :#{#req.phoneNumber} LIKE '' OR u.phone_number LIKE %:#{#req.phoneNumber}%)
            AND (:#{#req.status} IS NULL OR :#{#req.status} LIKE '' OR u.status = :#{#req.status})
            AND sr.rl_cd IN :listCdMentorMentee
            ORDER BY u.created_date DESC
            """, countQuery = """
            SELECT u.id
            FROM user u JOIN sys_user_role sur ON u.id = sur.user_id
            JOIN sys_role sr ON sr.rl_id = sur.rl_id
            WHERE (:#{#req.fullName} IS NULL OR :#{#req.fullName} LIKE '' OR u.full_name LIKE %:#{#req.fullName}% OR u.user_name LIKE %:#{#req.fullName}%)
            AND (:#{#req.email} IS NULL OR :#{#req.email} LIKE '' OR u.email LIKE %:#{#req.email}%)
            AND (:#{#req.role} IS NULL OR :#{#req.role} LIKE '' OR sr.rl_cd = :#{#req.role})
            AND (:#{#req.gender} IS NULL OR :#{#req.gender} LIKE '' OR u.gender = :#{#req.gender})
            AND (:#{#req.phoneNumber} IS NULL OR :#{#req.phoneNumber} LIKE '' OR u.phone_number LIKE %:#{#req.phoneNumber}%)
            AND (:#{#req.status} IS NULL OR :#{#req.status} LIKE '' OR u.status = :#{#req.status})
            AND sr.rl_cd IN :listCdMentorMentee
            ORDER BY u.created_date DESC
            """, nativeQuery = true)
    Page<UserMngListResponse> getPageUserMng(Pageable pageable, @Param ("req") UserMngSearchDTO req,
                                             @Param ("listCdMentorMentee") List<String> listCdMentorMentee);

    UserEntity findByUserName(String userName);

    @Query ("SELECT u FROM UserEntity u WHERE u.userName = :userName AND u.id <> :id")
    UserEntity findByUserNameNotInId(@Param ("userName") String userName, @Param ("id") String id);

    UserEntity findByEmail(String email);

    UserEntity findByEmailAndStatus(String email, String status);

    @Query(value = """
            SELECT 
                u.email AS email,
                u.full_name AS fullName,
                u.user_name AS userName,
                u.gender AS genderCd,
                COALESCE(FN_GET_CODE_NAME(u.gender), u.gender) AS genderName,
                u.date_of_birth AS dateOfBirth,
                u.phone_number AS phoneNumber,
                u.avatar AS avatar,
                u.description AS description,
                u.status AS statusCd,
                COALESCE(FN_GET_CODE_NAME(u.status), u.status) AS statusName
            FROM user u
            WHERE u.email = :email
            """, nativeQuery = true)
    UserProfileProjection getUserProfileByEmail(@Param("email") String email);

    UserEntity findByFacebookId(String facebookId);

    UserEntity findByFacebookIdAndStatus(String facebookId, String status);

}
