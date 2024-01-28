package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.common.session.SessionMemberInfo;
import org.c4marathon.assignment.member.dto.request.SignInRequestDto;
import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.dto.response.MemberInfoResponseDto;

public interface MemberService {
	void signUp(SignUpRequestDto requestDto);

	SessionMemberInfo signIn(SignInRequestDto requestDto);

	MemberInfoResponseDto getMemberInfo(long memberPk);
}
