package com.translateai.config.security.custom;

import com.translateai.common.Constants;
import com.translateai.entity.domain.UserEntity;
import com.translateai.repository.business.admin.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsImpl loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailAndStatus(email, Constants.CD_STATUS_01);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("User not found with username: " + email);
        }
        return UserDetailsImpl.buildUser(user);
    }

}
