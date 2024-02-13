package org.c4marathon.assignment.config.security;


import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.user.entity.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> ErrorCode.USER_NOT_EXIST.serviceException("Invalid authentication..."));

        return new CustomUserDetails(user);
    }
}
