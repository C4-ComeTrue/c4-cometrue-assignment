package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;

public interface MemberService {
	public void signUp(SignUpRequestDto requestDto);
}
