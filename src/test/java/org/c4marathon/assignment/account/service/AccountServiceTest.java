package org.c4marathon.assignment.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.TransferToSavingAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.SavingAccount;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.account.repository.SavingAccountRepository;
import org.c4marathon.assignment.auth.service.SecurityService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private SecurityService securityService;

    @Mock
    private MemberService memberService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SavingAccountRepository savingAccountRepository;

    @Nested
    @DisplayName("계좌 생성 테스트")
    class Create {
        @DisplayName("회원가입 이후 발생한 이벤트를 통해 메인 계좌를 생성한다.")
        @Test
        void createMainAccountTest() {
            // given
            Long memberId = 0L;
            Member member = mock(Member.class);
            Account account = mock(Account.class);
            given(memberService.getMemberById(memberId)).willReturn(member);
            given(accountRepository.save(any(Account.class))).willReturn(account);

            // when
            accountService.saveMainAccount(memberId);

            // then
            then(memberService).should(times(1)).getMemberById(memberId);
            then(accountRepository).should(times(1)).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("계좌 조회 테스트")
    class Read {

        @DisplayName("메인 계좌가 존재하는지 확인한다.")
        @Test
        void isMainAccountTest() {

            // given
            long memberId = 1L;
            given(accountRepository.existsAccountByMemberIdAndType(anyLong(), any())).willReturn(true);

            // when
            boolean result = accountService.isMainAccount(memberId);

            // then
            assertTrue(result);
        }

        @DisplayName("계좌 조회를 위해 회원의 정보를 조회하고, 회원의 메인 계좌 정보를 불러온다.")
        @Test
        void findAccountTest() {
            // given
            Long memberId = 1L;
            Account account = mock(Account.class);
            AccountResponseDto accountResponseDto = mock(AccountResponseDto.class);
            given(securityService.findMember()).willReturn(memberId);
            given(accountRepository.findByMemberId(memberId)).willReturn(account);

            // when
            AccountResponseDto afterAccountResponseDto = accountService.findAccount();

            // then
            then(securityService).should(times(1)).findMember();
            then(accountRepository).should(times(1)).findByMemberId(memberId);
            assertEquals(afterAccountResponseDto.id(), accountResponseDto.id());
        }
    }

    @Nested
    @DisplayName("계좌 충전 테스트")
    class Recharge {

        public static final int DAILY_LIMIT = 3_000_000;
        public static final Long memberId = 2L;
        public static final Long accountId = 2L;
        public static final Long balance = 10000L;

        @BeforeEach
        void setUp() {
            given(securityService.findMember()).willReturn(memberId);
        }

        @DisplayName("사용자의 외부 계좌에서 메인 계좌로 10,000원을 이체한다.")
        @Test
        void transferToRegularAccountTest() {

            // given
            Account account = mock(Account.class);
            RechargeAccountRequestDto requestDto = new RechargeAccountRequestDto(accountId, balance);

            given(accountRepository.findByRegularAccount(memberId)).willReturn(Optional.of(account));
            given(account.getDailyLimit()).willReturn(0);
            given(account.getBalance()).willReturn(0L);

            // when
            accountService.rechargeAccount(requestDto);

            // then
            verify(account, times(1)).resetDailyLimit(anyInt());
            verify(account, times(1)).transferBalance(anyLong());
            verify(accountRepository, times(1)).save(account);
        }

        @DisplayName("메인 계좌에 잔액 충전 요청 시 충전 한도가 넘어 오류가 발생한다.")
        @Test
        void rechargeAccountErrorTest() {

            // given
            Account account = mock(Account.class);
            RechargeAccountRequestDto requestDto = new RechargeAccountRequestDto(accountId, balance);

            given(accountRepository.findByRegularAccount(memberId)).willReturn(Optional.of(account));
            given(account.getDailyLimit()).willReturn(DAILY_LIMIT);
            given(account.getBalance()).willReturn(0L);

            // when
            Exception exception = assertThrows(BaseException.class, () -> accountService.rechargeAccount(requestDto));

            // then
            assertEquals(HttpStatus.BAD_REQUEST.toString(), exception.getMessage());
        }

        @DisplayName("메인 계좌에서 적금 계좌로 이체 요청 시 입력한 금액만큼 충전된다.")
        @Test
        void transferFromRegularAccountTest() {

            // given
            Long receiverAccountd = 3L;
            Account regularAccount = mock(Account.class);
            SavingAccount savingAccount = mock(SavingAccount.class);
            TransferToSavingAccountRequestDto requestDto = new TransferToSavingAccountRequestDto(balance,
                receiverAccountd);

            given(regularAccount.getBalance()).willReturn(balance);
            given(accountRepository.findByRegularAccount(memberId)).willReturn(Optional.of(regularAccount));
            given(savingAccountRepository.findBySavingAccount(memberId, requestDto.receiverAccountId())).willReturn(
                Optional.of(savingAccount));

            // when
            accountService.transferFromRegularAccount(requestDto);

            // then
            then(accountRepository).should(times(1)).findByRegularAccount(memberId);
            then(savingAccountRepository).should(times(1))
                .findBySavingAccount(memberId, requestDto.receiverAccountId());
        }

        @DisplayName("메인 계좌에서 적금 계좌로 입금 시 잔액 부족하다면 오류가 발생한다.")
        @Test
        void transferFromRegularAccountErrorTest() {

            // given
            Account regularAccount = mock(Account.class);
            TransferToSavingAccountRequestDto requestDto = new TransferToSavingAccountRequestDto(balance, accountId);

            given(regularAccount.getBalance()).willReturn(0L);
            given(accountRepository.findByRegularAccount(memberId)).willReturn(Optional.of(regularAccount));

            // when
            Exception exception = assertThrows(BaseException.class,
                () -> accountService.transferFromRegularAccount(requestDto));

            // then
            assertEquals(HttpStatus.FORBIDDEN.toString(), exception.getMessage());
        }
    }
}