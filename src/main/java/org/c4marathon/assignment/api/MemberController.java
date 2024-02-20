package org.c4marathon.assignment.api;

import org.c4marathon.assignment.api.dto.MemberSignUpDto;
import org.c4marathon.assignment.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/sign-up")
	@ResponseStatus(HttpStatus.CREATED)
	public MemberSignUpDto.Res register(@Valid @RequestBody MemberSignUpDto.Req req) {
		return memberService.register(req.email(), req.password());
	}
}
