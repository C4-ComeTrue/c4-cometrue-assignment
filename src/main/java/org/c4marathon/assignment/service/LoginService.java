package org.c4marathon.assignment.service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.hash.Hashing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

	private final MemberRepository memberRepository;

	public void login(String userId, String password, HttpServletRequest request) {

		// TO-DO 패스워드 암/복호화
		// https://seed.kisa.or.kr/kisa/Board/38/detailView.do KISA 암/복호화 가이드라인 규칙을 따르고자 함.
		// 보안강도 112비트 이상 필요

		Optional<Member> findMemberOptional = memberRepository.findMemberByUserId(userId);
		String encPwd = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();

		if (findMemberOptional.isEmpty()) {
			throw ErrorCd.NOT_EXIST_USER.serviceException("아이디 비밀번호를 다시 확인하세요",
				"존재하지 않는 아이디");
		}

		if (!findMemberOptional.get().getPassword().equals(encPwd)) {
			throw ErrorCd.NOT_EXIST_USER.serviceException("아이디 비밀번호를 다시 확인하세요",
				"비밀번호가 맞지 않음");
		}

		Member loginMember = findMemberOptional.get();
		HttpSession session = request.getSession();
		session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

	}

	public void logout(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.invalidate();
	}


}
