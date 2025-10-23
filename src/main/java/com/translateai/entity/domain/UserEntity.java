package com.translateai.entity.domain;

import com.translateai.constant.classconstant.EntityProperties;
import com.translateai.entity.base.PrimaryEntity;
import com.translateai.entity.system.RoleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Nationalized;

import java.util.Date;
import java.util.List;

/**
 * @author HoangDV
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@FieldDefaults (level = AccessLevel.PRIVATE)
@Entity
@Table (name = "user")
@ToString
public class UserEntity extends PrimaryEntity {

    @Column (length = EntityProperties.LENGTH_EMAIL)
    String email;

    @Column (length = EntityProperties.LENGTH_PASSWORD_ENCODER)
    String password;

    @Column (length = EntityProperties.LENGTH_NAME)
    @Nationalized
    String fullName;

    @Column (length = EntityProperties.LENGTH_CODE)
    String userName;

    @Column (length = EntityProperties.LENGTH_CODE)
    String gender;

    @Column
    Date dateOfBirth;

    @Column (length = EntityProperties.LENGTH_PHONE)
    String phoneNumber;

    @Column (length = EntityProperties.LENGTH_URL)
    String avatar;

    @Column (length = EntityProperties.LENGTH_DESCRIPTION)
    @Nationalized
    String description;

    @Column (length = EntityProperties.REFER_CODE)
    String myReferCode;

    @Column (length = EntityProperties.LENGTH_CODE)
    String status;

    @Column (length = EntityProperties.LENGTH_CODE)
    String loginType;

    @Column (length = EntityProperties.LENGTH_USERNAME)
    String facebookId;

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable (name = "sys_user_role", joinColumns = @JoinColumn (name = "user_id"), inverseJoinColumns = @JoinColumn (name = "rl_id"))
    private List<RoleEntity> roles;

}
