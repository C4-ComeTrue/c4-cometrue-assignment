package org.c4marathon.assignment.service;

import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	AccountRepository accountRepository;

	@InjectMocks
	MemberService memberService;

	@Test
	void 회원가입에_성공한다() {
		// given
		var email = "ss@naver.com";
		var password = "@@XX";
		var member = mock(Member.class);
		var memberId = 1L;

		given(memberRepository.save(any())).willReturn(member);
		given(member.getId()).willReturn(memberId);

		// when
		var result = memberService.register(email, password);

		// then
		Assertions.assertThat(result.id()).isEqualTo(memberId);
	}

}
