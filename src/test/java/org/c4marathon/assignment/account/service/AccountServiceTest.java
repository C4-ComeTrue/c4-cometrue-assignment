package org.c4marathon.assignment.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.TransferToOtherAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.SavingAccount;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.account.repository.SavingAccountRepository;
import org.c4marathon.assignment.auth.service.SecurityService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
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

            given(accountRepository.findByAccount(memberId)).willReturn(Optional.of(account));
            given(account.getDailyLimit()).willReturn(0);
            given(account.getBalance()).willReturn(0L);

            // when
            accountService.rechargeAccount(requestDto);

            // then
            verify(account, times(1)).resetDailyLimit(anyInt());
            verify(account, times(1)).transferBalance(anyLong());
            verify(accountRepository, times(1)).save(account);
        }

        @DisplayName("사용자의 외부 계좌에서 메인 계좌로 10,000원을 이체할 때 조회한 한도가 기준치를 넘었지만, 직전 입금 날짜가 달라 초기화하고 성공적으로 입금된다.")
        @Test
        void transferToRegularAccountUpdateAtTest() {

            // given
            Account account = mock(Account.class);
            RechargeAccountRequestDto requestDto = new RechargeAccountRequestDto(accountId, balance);

            given(accountRepository.findByAccount(memberId)).willReturn(Optional.of(account));
            given(account.getDailyLimit()).willReturn(3000000);
            given(account.getBalance()).willReturn(0L);
            given(account.getDailyLimitUpdateAt()).willReturn(LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1));

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

            given(accountRepository.findByAccount(memberId)).willReturn(Optional.of(account));
            given(account.getDailyLimit()).willReturn(DAILY_LIMIT);
            given(account.getBalance()).willReturn(0L);
            given(account.getDailyLimitUpdateAt()).willReturn(LocalDate.now(ZoneId.of("Asia/Seoul")));

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
            TransferToOtherAccountRequestDto requestDto = new TransferToOtherAccountRequestDto(balance,
                receiverAccountd);

            given(regularAccount.getBalance()).willReturn(balance);
            given(accountRepository.findByAccount(memberId)).willReturn(Optional.of(regularAccount));
            given(savingAccountRepository.findBySavingAccount(memberId, requestDto.receiverAccountId())).willReturn(
                Optional.of(savingAccount));

            // when
            accountService.transferFromRegularAccount(requestDto);

            // then
            then(accountRepository).should(times(1)).findByAccount(memberId);
            then(savingAccountRepository).should(times(1))
                .findBySavingAccount(memberId, requestDto.receiverAccountId());
        }

        @DisplayName("메인 계좌에서 적금 계좌로 입금 시 잔액 부족하다면 오류가 발생한다.")
        @Test
        void transferFromRegularAccountErrorTest() {

            // given
            Account regularAccount = mock(Account.class);
            TransferToOtherAccountRequestDto requestDto = new TransferToOtherAccountRequestDto(balance, accountId);

            given(regularAccount.getBalance()).willReturn(0L);
            given(accountRepository.findByAccount(memberId)).willReturn(Optional.of(regularAccount));

            // when
            Exception exception = assertThrows(BaseException.class,
                () -> accountService.transferFromRegularAccount(requestDto));

            // then
            assertEquals(HttpStatus.FORBIDDEN.toString(), exception.getMessage());
        }

        @DisplayName("메인 계좌에서 적금 계좌로 이체 요청 시 송금한다.")
        @Test
        void transferToOtherAccountTest() {

            // given
            Long receiverAccountd = 3L;
            TransferToOtherAccountRequestDto requestDto = new TransferToOtherAccountRequestDto(balance,
                receiverAccountd);

            Account account = mock(Account.class);
            given(accountRepository.findByAccount(memberId)).willReturn(Optional.of(account));

            Account otherAccount = mock(Account.class);
            given(account.getBalance()).willReturn(balance);
            given(accountRepository.findByOtherAccount(requestDto.receiverAccountId())).willReturn(
                Optional.of(otherAccount));

            // when
            accountService.transferToOtherAccount(requestDto);

            // then
            then(accountRepository).should(times(1)).findByAccount(memberId);
            then(accountRepository).should(times(1)).findByOtherAccount(requestDto.receiverAccountId());
        }

        @DisplayName("메인 계좌에서 적금 계좌로 이체 요청 시 잔액이 부족해 부족한 금액을 충전하고 송금한다.")
        @Test
        void transferToOtherAccountAndChargeAccountTest() {

            // given
            Long receiverAccountd = 3L;
            TransferToOtherAccountRequestDto requestDto = new TransferToOtherAccountRequestDto(balance * 2,
                receiverAccountd);

            Account account = mock(Account.class);
            given(accountRepository.findByAccount(memberId)).willReturn(Optional.of(account));

            Account otherAccount = mock(Account.class);
            given(account.getBalance()).willReturn(balance);
            given(accountRepository.findByOtherAccount(requestDto.receiverAccountId())).willReturn(
                Optional.of(otherAccount));

            // when
            accountService.transferToOtherAccount(requestDto);

            // then
            then(accountRepository).should(times(2)).findByAccount(memberId);
            then(accountRepository).should(times(1)).findByOtherAccount(requestDto.receiverAccountId());
        }

        @DisplayName("친구의 계좌가 존재하지 않거나 잘못 입력해 실패한다.")
        @Test
        void transferToOtherAccountFailedTest() {

            // given
            Long receiverAccountd = 3L;
            TransferToOtherAccountRequestDto requestDto = new TransferToOtherAccountRequestDto(balance * 2,
                receiverAccountd);

            Account account = mock(Account.class);
            given(accountRepository.findByAccount(memberId)).willReturn(Optional.of(account));
            given(account.getBalance()).willReturn(balance);

            BaseException baseException = ErrorCode.ACCOUNT_DOES_NOT_EXIST.baseException("계좌가 존재하지 않습니다.");
            given(accountRepository.findByOtherAccount(requestDto.receiverAccountId())).willThrow(baseException);

            // when
            Exception exception = assertThrows(BaseException.class,
                () -> accountService.transferToOtherAccount(requestDto));

            // then
            assertEquals(baseException.getMessage(), exception.getMessage());
        }
    }
}