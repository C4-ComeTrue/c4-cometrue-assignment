package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.dto.request.PostUserReq;
import org.c4marathon.assignment.entity.Account;
import org.c4marathon.assignment.entity.User;
import org.c4marathon.assignment.exception.CustomException;
import org.c4marathon.assignment.exception.ErrorCode;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void registerUser(PostUserReq postUserReq) {
        if (isEmailExist(postUserReq.email())) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        User user = userRepository.save(User.builder()
                .username(postUserReq.username())
                .email(postUserReq.email())
                .nickname(postUserReq.nickname())
                .build());

        Account mainAccount = new Account(user.getId());
        accountRepository.save(mainAccount);
    }

    public boolean isEmailExist(String email) {
        return userRepository.isEmailExist(email);
    }
}
