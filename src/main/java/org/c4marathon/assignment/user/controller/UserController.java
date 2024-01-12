package org.c4marathon.assignment.user.controller;

import org.c4marathon.assignment.user.dto.CreateUserRequest;
import org.c4marathon.assignment.user.service.UserService;
import org.springframework.http.HttpStatus;
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

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody CreateUserRequest dto) {
        userService.createUser(dto.userName(), dto.userEmail(), dto.password());
    }
}
