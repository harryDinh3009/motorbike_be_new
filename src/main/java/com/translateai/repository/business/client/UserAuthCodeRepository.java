package com.translateai.repository.business.client;

import com.translateai.entity.domain.UserAuthCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthCodeRepository extends JpaRepository<UserAuthCodeEntity, String> {

    UserAuthCodeEntity findByEmail(String email);

}
