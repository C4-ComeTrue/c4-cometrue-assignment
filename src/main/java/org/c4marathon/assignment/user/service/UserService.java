package org.c4marathon.assignment.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.JoinDto;
import org.c4marathon.assignment.user.dto.LoginDto;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 회원가입
    @Transactional
    public boolean save(JoinDto joinDto){
        Optional checkUser = userRepository.findByUserId(joinDto.userId());

        if(checkUser == null){
            User user = User.builder()
                    .userId(joinDto.userId())
                    .userPw(joinDto.userPw())
                    .name(joinDto.name())
                    .build();

            userRepository.save(user);
            return true;
        }else {
            return false;
        }
    }

    // 로그인
    public LoginDto login(LoginDto loginDto){
        Optional<User> userRepositoryByUserId = userRepository.findByUserId(loginDto.userId());
        if (userRepositoryByUserId.isPresent()) {
            User userEntity = userRepositoryByUserId.get();

            if (userEntity.getUserPw().equals(loginDto.userPw())) {
                return loginDto;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
