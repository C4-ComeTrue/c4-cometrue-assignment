package org.c4marathon.assignment.user.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.user.entity.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void createUser(String userName, String userEmail, String password) {
        User user = User.builder()
                .userName(userName)
                .userEmail(userEmail)
                .password(password)
                .build();

        userRepository.save(user);
    }
}
