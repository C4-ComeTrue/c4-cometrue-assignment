package org.c4marathon.assignment.user.controller;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.config.CommonResponse;
import org.c4marathon.assignment.user.dto.JoinDto;
import org.c4marathon.assignment.user.dto.LoginDto;
import org.c4marathon.assignment.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("user/join")
    public ResponseEntity<CommonResponse> join(
            @RequestBody JoinDto joinDto
    ){

        boolean checkJoin = userService.save(joinDto);

        if(checkJoin == true){
            CommonResponse res = new CommonResponse(
                    200,
                    HttpStatus.OK,
                    "회원가입 성공",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }else {
            CommonResponse res = new CommonResponse(
                    400,
                    HttpStatus.BAD_REQUEST,
                    "회원가입 실패",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }
    }


    // 로그인
    @PostMapping("user/login")
    public ResponseEntity<CommonResponse> login(
            @RequestBody LoginDto loginDto
    ){
        LoginDto loginResult = userService.login(loginDto);

        if(loginResult != null){
            CommonResponse res = new CommonResponse(
                    200,
                    HttpStatus.OK,
                    "로그인 성공",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }else {
            CommonResponse res = new CommonResponse(
                    400,
                    HttpStatus.BAD_REQUEST,
                    "로그인 실패",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }
    }

}
