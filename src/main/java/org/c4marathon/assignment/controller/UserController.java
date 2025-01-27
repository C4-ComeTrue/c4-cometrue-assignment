package org.c4marathon.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.dto.request.PostUserReq;
import org.c4marathon.assignment.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping()
    public ResponseEntity<Void> registerUser(@RequestBody @Valid PostUserReq postUserReq) {
        userService.registerUser(postUserReq);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
