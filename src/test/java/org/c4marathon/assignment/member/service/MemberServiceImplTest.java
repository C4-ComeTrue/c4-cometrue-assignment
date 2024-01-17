package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.exception.MemberException;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

	@Mock
	MemberRepository memberRepository;

	@Nested
	@DisplayName("회원 가입 테스트")
	class SignUp {

		@Test
		@DisplayName("중복되는 아이디가가 없으면 회원가입에 성공한다.")
		void request_with_non_duplicated_id() {

			// Given
			MemberService memberService = new MemberServiceImpl(memberRepository);
			SignUpRequestDto requestDto = makeRequestForm();
			given(memberRepository.findMemberByMemberId(requestDto.memberId())).willReturn(null);

			// When
			memberService.signUp(requestDto);

			// Then
			then(memberRepository).should(times(1)).findMemberByMemberId(requestDto.memberId());
		}

		@Test
		@DisplayName("이미 가입된 아이디면 MemberException 예외가 발생한다")
		void request_with_duplicated_id() {
			//Given
			MemberService memberService = new MemberServiceImpl(memberRepository);
			SignUpRequestDto requestDto = makeRequestForm();
			Member member = requestDto.toEntity();
			given(memberRepository.findMemberByMemberId(requestDto.memberId())).willReturn(member);

			// When
			MemberException memberException = assertThrows(
				MemberException.class, () -> {
					memberService.signUp(requestDto);
				});

			// Then
			assertEquals("USER_ALREADY_EXIST", memberException.getErrorName());
		}

		private SignUpRequestDto makeRequestForm() {
			return new SignUpRequestDto("seungh1024", "testPass", "testName", "01012345678");
		}
	}
}
