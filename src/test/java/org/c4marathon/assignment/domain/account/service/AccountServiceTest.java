package org.c4marathon.assignment.domain.account.service;

import org.c4marathon.assignment.domain.account.dto.DepositDto;
import org.c4marathon.assignment.domain.account.dto.TransferDto;
import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.entity.SavingAccount;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.account.repository.SavingAccountRepository;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private SavingAccountRepository savingAccountRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AccountService accountService;

    @Test
    @DisplayName("유효한 이메일로 계좌 생성에 성공")
    void createAccountSuccess(){
        String email = "test@naver.com";
        User user = mock(User.class);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        Account account = new Account(user);
        given(accountRepository.save(any(Account.class))).willReturn(account);

        // when
        accountService.createAccount(email);

        // then
        verify(userRepository).findByEmail(email);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("유효하지 않은 이메일로 계좌 생성 시 예외 발생")
    void createAccountInvalidEmail() {
        // given
        String email = "invalid@naver.com";
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            accountService.createAccount(email);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_EMAIL);
        verify(userRepository).findByEmail(email);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("적금 계좌 생성에 성공")
    void createSavingAccountSuccess(){
        String email = "test@naver.com";
        User user = mock(User.class);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        SavingAccount account = new SavingAccount(user);
        given(savingAccountRepository.save(any(SavingAccount.class))).willReturn(account);

        // when
        accountService.createSavingAccount(email);

        // then
        verify(userRepository).findByEmail(email);
        verify(savingAccountRepository).save(any(SavingAccount.class));
    }


    @Test
    @DisplayName("인당 한도 초과하지 않았다면 메인 계좌에 충전 성공")
    void depositToMainAccountSuccess(){
        // given
        long amount = 1000L;
        long mainAccountId = 1L;

        User user = mock(User.class);
        Account mainAccount = mock(Account.class);

        given(user.getMainAccountId()).willReturn(mainAccountId);
        given(mainAccount.getDailyTopUpAmount()).willReturn(0L);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(accountRepository.findByIdWithWriteLock(mainAccountId)).willReturn(Optional.of(mainAccount));

        // when
        DepositDto.Res res = accountService.depositToMainAccount("test@naver.com", amount);

        // then
        verify(mainAccount).deposit(amount);
        assertThat(res.balance()).isEqualTo(mainAccount.getBalance());
    }


    @Test
    @DisplayName("인당 한도 초과한다면 메인 계좌에 충전 실패")
    void depositToMainAccountLimitExceed(){
        // given
        long amount = 1000L;
        long mainAccountId = 1L;

        User user = mock(User.class);
        Account mainAccount = mock(Account.class);

        given(user.getMainAccountId()).willReturn(mainAccountId);
        given(mainAccount.getDailyTopUpAmount()).willReturn(3_000_000L);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(accountRepository.findByIdWithWriteLock(mainAccountId)).willReturn(Optional.of(mainAccount));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            accountService.depositToMainAccount("test@naver.com", amount);
        });

        // then
        assertEquals(ErrorCode.DEPOSIT_LIMIT_EXCEED, exception.getErrorCode());
        verify(mainAccount, never()).deposit(amount);
    }

    @Test
    @DisplayName("계좌의 잔액이 충분하다면 적금 계좌에 이체 성공")
    void transferToSavingsSuccess(){
        // given
        long amount = 1000L;
        long mainAccountId = 1L;
        long savingAccountId = 1L;

        User user = mock(User.class);
        Account account = mock(Account.class);
        SavingAccount savingAccount = mock(SavingAccount.class);

        given(user.getMainAccountId()).willReturn(mainAccountId);
        given(account.getBalance()).willReturn(2000L);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(accountRepository.findByIdWithWriteLock(mainAccountId)).willReturn(Optional.of(account));
        given(savingAccountRepository.findByIdWithWriteLock(savingAccountId)).willReturn(Optional.of(savingAccount));

        // when
        TransferDto.Res res = accountService.transferToSavings("test@naver.com", savingAccountId, amount);

        // then
        verify(account).withdraw(amount);
        verify(savingAccount).deposit(amount);
        assertThat(res.balance()).isEqualTo(savingAccount.getBalance());
    }

    @Test
    @DisplayName("계좌의 잔액이 충분하지 않으면 적금 계좌에 이체 실패")
    void transferToSavingsBalanceExceed(){
        // given
        long amount = 3000L;
        long mainAccountId = 1L;
        long savingAccountId = 1L;

        User user = mock(User.class);
        Account account = mock(Account.class);

        given(user.getMainAccountId()).willReturn(mainAccountId);
        given(account.getBalance()).willReturn(2000L);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(accountRepository.findByIdWithWriteLock(mainAccountId)).willReturn(Optional.of(account));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            TransferDto.Res res = accountService.transferToSavings("test@naver.com", savingAccountId, amount);
        });

        // then
        assertEquals(ErrorCode.MAIN_ACCOUNT_BALANCE_EXCEED, exception.getErrorCode());
        verify(account, never()).withdraw(amount);
    }


}