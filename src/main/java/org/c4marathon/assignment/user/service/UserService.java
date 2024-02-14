package org.c4marathon.assignment.user.service;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.config.security.JwtProvider;
import org.c4marathon.assignment.user.dto.LoginResponse;
import org.c4marathon.assignment.user.entity.Authority;
import org.c4marathon.assignment.user.entity.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 사용자 회원가입 API
     * @param userName
     * @param userEmail
     * @param password
     * @throws Exception
     */
    public void signUp(String userName, String userEmail, String password){
            User user = User.builder()
                    .userName(userName)
                    .userEmail(userEmail)
                    .password(passwordEncoder.encode(password))
                    .build();

            user.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
            userRepository.save(user);
    }

    /**
     * 사용자 로그인 API
     * @param userEmail
     * @param password
     * @return
     * @throws Exception
     */
    public LoginResponse login(String userEmail, String password) {
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> ErrorCode.USER_NOT_EXIST.serviceException());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw ErrorCode.USER_PASSWORD_MATCH_ERROR.serviceException();
        }

        return LoginResponse.builder()
            .id(user.getId())
            .userEmail(user.getUserEmail())
            .userName(user.getUserName())
            .roles(user.getRoles())
            .token(jwtProvider.createToken(user.getUserEmail(), user.getRoles()))
            .build();
    }

    public User getUser(String userEmail) {
        return userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> ErrorCode.USER_NOT_EXIST.serviceException());
    }
}
