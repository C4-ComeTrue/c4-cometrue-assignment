package org.c4marathon.assignment.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.TransferToSavingAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.auth.service.SecurityService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private SecurityService securityService;

    @Mock
    private AccountRepository accountRepository;

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

        @DisplayName("계좌 조회를 위해 회원의 정보를 조회하고, 회원의 모든 계좌 정보를 불러온다.")
        @Test
        void findAccountTest() {
            // given
            Long memberId = 1L;
            List<Account> accountList = List.of(mock(Account.class), mock(Account.class));
            Authentication authentication = mock(Authentication.class);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            given(securityService.findMember()).willReturn(memberId);
            given(accountRepository.findByMemberId(memberId)).willReturn(accountList);

            // when
            List<AccountResponseDto> result = accountService.findAccount();

            // then
            assertEquals(accountList.size(), result.size());  // 반환된 계좌 리스트의 크기는 `accountList`의 크기와 같아야 함
            verify(accountRepository, times(1)).findByMemberId(memberId);  // `findByMemberId` 메서드가 한 번 호출되어야 함
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
            Authentication authentication = mock(Authentication.class);
            given(securityService.findMember()).willReturn(memberId);
            SecurityContextHolder.getContext().setAuthentication(authentication);
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
            Exception exception = assertThrows(BaseException.class, () -> {
                accountService.rechargeAccount(requestDto);
            });

            // then
            assertEquals(HttpStatus.BAD_REQUEST.toString(), exception.getMessage());
        }

        @DisplayName("메인 계좌에서 적금 계좌로 이체 요청 시 입력한 금액만큼 충전된다.")
        @Test
        void transferFromRegularAccountTest() {

            // given
            Long receiverAccountd = 3L;
            Account regularAccount = mock(Account.class);
            Account savingAccount = mock(Account.class);
            TransferToSavingAccountRequestDto requestDto = new TransferToSavingAccountRequestDto(balance, receiverAccountd);

            given(regularAccount.getBalance()).willReturn(balance);
            given(accountRepository.findByRegularAccount(memberId)).willReturn(Optional.of(regularAccount));
            given(accountRepository.findByAccount(memberId, requestDto.receiverAccountId())).willReturn(
                Optional.of(savingAccount));

            // when
            accountService.transferFromRegularAccount(requestDto);

            // then
            verify(regularAccount, times(1)).transferBalance(anyLong());
            verify(savingAccount, times(1)).transferBalance(anyLong());
            verify(accountRepository, times(1)).saveAll(anyList());
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
            Exception exception = assertThrows(BaseException.class, () -> {
                accountService.transferFromRegularAccount(requestDto);
            });

            // then
            assertEquals(HttpStatus.FORBIDDEN.toString(), exception.getMessage());
        }
    }
}