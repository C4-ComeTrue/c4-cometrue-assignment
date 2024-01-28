package org.c4marathon.assignment.member.service;

import java.util.Optional;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.limit.ChargeLimitManager;
import org.c4marathon.assignment.bankaccount.limit.LimitConst;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.member.dto.request.SignInRequestDto;
import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.dto.response.MemberInfoResponseDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.exception.MemberErrorCode;
import org.c4marathon.assignment.member.exception.MemberException;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

	@InjectMocks
	MemberServiceImpl memberService;
	@Mock
	MemberRepository memberRepository;
	@Mock
	ChargeLimitManager chargeLimitManager;
	@Mock
	MainAccountRepository mainAccountRepository;

	@Nested
	@DisplayName("회원 가입 테스트")
	class SignUp {

		@Test
		@DisplayName("중복되는 아이디가 없으면 회원가입에 성공한다.")
		void request_with_non_duplicated_id() {

			// Given
			SignUpRequestDto requestDto = makeRequestForm();
			given(memberRepository.findMemberByMemberId(requestDto.memberId())).willReturn(null);
			MainAccount mainAccount = new MainAccount();

			given(mainAccountRepository.save(any())).willReturn(mainAccount);
			given(chargeLimitManager.get(mainAccount.getAccountPk())).willReturn(LimitConst.CHARGE_LIMIT);

			// When
			memberService.signUp(requestDto);

			// Then
			then(memberRepository).should(times(1)).findMemberByMemberId(requestDto.memberId());
			then(mainAccountRepository).should(times(1)).save(any());
			then(chargeLimitManager).should(times(1)).init(anyLong());
			assertEquals(chargeLimitManager.get(mainAccount.getAccountPk()), LimitConst.CHARGE_LIMIT);
		}

		@Test
		@DisplayName("이미 가입된 아이디면 MemberException 예외가 발생한다")
		void request_with_duplicated_id() {
			//Given
			SignUpRequestDto requestDto = makeRequestForm();
			Member member = requestDto.toEntity();
			given(memberRepository.findMemberByMemberId(requestDto.memberId())).willReturn(member);

			// When
			MemberException memberException = assertThrows(MemberException.class, () -> {
				memberService.signUp(requestDto);
			});

			// Then
			assertEquals(MemberErrorCode.USER_ALREADY_EXIST.name(), memberException.getErrorName());
		}

		private SignUpRequestDto makeRequestForm() {
			return new SignUpRequestDto("seungh1024", "testPass", "testName", "01012345678");
		}

	}

	@Nested
	@DisplayName("로그인 서비스")
	class SignIn {
		@Test
		@DisplayName("입력한 아이디와 비밀번호가 가입한 정보와 일치하면 회원가입에 성공한다.")
		void request_with_valid_user_info() {
			// Given
			SignInRequestDto requestDto = new SignInRequestDto("testId", "password");
			Member member = Member.builder()
				.memberId("testId")
				.password("password")
				.memberName("testName")
				.phoneNumber("01012345678")
				.build();
			given(memberRepository.findMemberByMemberId(requestDto.memberId())).willReturn(member);

			// When
			memberService.signIn(requestDto);

			// Then
			then(memberRepository).should(times(1)).findMemberByMemberId(requestDto.memberId());

		}

		@Test
		@DisplayName("입력한 아이디가 존재하지 않으면 MemberException(NOT_FOUND) 예외가 발생한다.")
		void request_with_not_exist_id() {
			// Given
			SignInRequestDto requestDto = new SignInRequestDto("not exist id", "password");
			given(memberRepository.findMemberByMemberId(requestDto.memberId())).willReturn(null);

			// When
			MemberException memberException = assertThrows(MemberException.class, () -> {
				memberService.signIn(requestDto);
			});

			// Then
			assertEquals(MemberErrorCode.USER_NOT_FOUND.name(), memberException.getErrorName());
		}

		@Test
		@DisplayName("입력한 비밀번호가 일치하지 않으면 MemberException(INVALID_PASSWORD) 예외가 발생한다.")
		void request_with_invalid_password() {
			// Given
			SignInRequestDto requestDto = new SignInRequestDto("testId", "invalid password");
			Member member = Member.builder()
				.memberId("testId")
				.password("password")
				.memberName("testName")
				.phoneNumber("01012345678")
				.build();
			given(memberRepository.findMemberByMemberId(requestDto.memberId())).willReturn(member);

			// When
			MemberException memberException = assertThrows(MemberException.class, () -> {
				memberService.signIn(requestDto);
			});

			// Then
			assertEquals(MemberErrorCode.INVALID_PASSWORD.name(), memberException.getErrorName());
		}
	}

	@Nested
	@DisplayName("사용자 정보 테스트")
	class GetMemberInfo {
		@Test
		@DisplayName("로그인한 사용자의 요청에는 사용자의 정보를 반환한다.")
		void request_with_login_member() {
			// Given
			long memberPk = 1L;
			Member member = Member.builder()
				.memberId("testId")
				.password("password")
				.memberName("testName")
				.phoneNumber("01012345678")
				.build();
			given(memberRepository.findById(memberPk)).willReturn(Optional.of(member));

			// When
			MemberInfoResponseDto memberInfo = memberService.getMemberInfo(memberPk);

			// Then
			assertEquals(memberInfo.memberPk(), member.getMemberPk());
			assertEquals(memberInfo.memberId(), member.getMemberId());
			assertEquals(memberInfo.memberName(), member.getMemberName());
		}

		@Test
		@DisplayName("존재하지 않는 memberPk로 요청을 보내면 MemberException(USER_NOT_FOUND) 예외가 발생한다")
		void request_with_not_exist_memberPk() {
			// Given
			long memberPk = 1L;
			given(memberRepository.findById(memberPk)).willThrow(MemberErrorCode.USER_NOT_FOUND.memberException());

			// When
			MemberException memberException = assertThrows(MemberException.class, () -> {
				memberService.getMemberInfo(memberPk);
			});

			// Then
			assertEquals(MemberErrorCode.USER_NOT_FOUND.name(), memberException.getErrorName());

		}
	}
}
