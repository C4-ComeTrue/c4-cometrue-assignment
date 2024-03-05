package org.c4marathon.assignment.domain.auth.controller;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.auth.service.AuthService;
import org.c4marathon.assignment.global.constant.MemberType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/signup")
	public void signup(@RequestBody @Valid SignUpRequest request, @RequestParam MemberType memberType) {
		authService.signup(request, memberType);
	}
}
