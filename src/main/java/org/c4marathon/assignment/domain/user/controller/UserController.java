package org.c4marathon.assignment.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.user.dto.SignUpDto;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpDto.Res> signUp(@RequestBody @Valid SignUpDto.Req req){
        return new ResponseEntity<>(userService.signUp(req.email(), req.password()), HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignUpDto.Res> signIn(@RequestBody @Valid SignUpDto.Req req){
        return new ResponseEntity<>(userService.signIn(req.email(), req.password()), HttpStatus.OK);
    }

}
