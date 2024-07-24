package org.c4marathon.assignment.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.user.dto.SignUpDto;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;
import org.c4marathon.assignment.global.jwt.JwtTokenProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public SignUpDto.Res signUp(String email, String password){
        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL); // 이미 존재하는 이메일이 있는 경우
        }

        User user = userRepository.save(new User(email, BCrypt.hashpw(password, BCrypt.gensalt())));

        Account mainAccount = new Account(user);
        Account savedMainAccount = accountRepository.save(mainAccount);

        user.updateMainAccount(savedMainAccount.getId());
        return new SignUpDto.Res(tokenProvider.createAccessToken(user.getEmail(), "ROLE_USER"));
    }

    @Transactional
    public SignUpDto.Res signIn(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        if (!BCrypt.checkpw(password, user.getPassword())) throw new CustomException(ErrorCode.INVALID_PASSWORD);

        return new SignUpDto.Res(tokenProvider.createAccessToken(user.getEmail(), "ROLE_USER"));
    }
}
