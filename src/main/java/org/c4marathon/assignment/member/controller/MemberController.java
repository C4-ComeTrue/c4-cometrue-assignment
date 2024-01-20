package org.c4marathon.assignment.member.controller;

import org.c4marathon.assignment.common.annotation.Login;
import org.c4marathon.assignment.common.session.SessionConst;
import org.c4marathon.assignment.common.session.SessionMemberInfo;
import org.c4marathon.assignment.member.dto.request.SignInRequestDto;
import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.dto.response.MemberInfo;
import org.c4marathon.assignment.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/signup")
	public void signup(@RequestBody @Valid SignUpRequestDto requestDto) {
		memberService.signUp(requestDto);
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/signin")
	public void signin(@RequestBody @Valid SignInRequestDto requestDto, HttpServletRequest request) {
		SessionMemberInfo memberDto = memberService.signIn(requestDto);

		HttpSession session = request.getSession();
		session.setAttribute(SessionConst.MEMBER_INFO, memberDto);
		System.out.println(session.getAttribute(SessionConst.MEMBER_INFO));
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/info")
	public MemberInfo getMyInfo(@Login SessionMemberInfo memberInfo) {
		return memberService.getMemberInfo(memberInfo.memberPk());
	}

}
