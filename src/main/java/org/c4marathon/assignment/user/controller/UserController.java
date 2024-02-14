package org.c4marathon.assignment.user.controller;

import org.c4marathon.assignment.user.dto.LoginRequest;
import org.c4marathon.assignment.user.dto.LoginResponse;
import org.c4marathon.assignment.user.dto.SignUpRequest;
import org.c4marathon.assignment.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 회원가입 API
     * @param dto
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody SignUpRequest dto) {
        userService.signUp(dto.userName(), dto.userEmail(), dto.password());
    }

    /**
     * 사용자 로그인 API
     * @param dto
     * @return accessToken
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest dto) {
        return new ResponseEntity<>(userService.login(dto.userEmail(), dto.password()), HttpStatus.OK);
    }
}
