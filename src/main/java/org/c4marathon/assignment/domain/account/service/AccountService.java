package org.c4marathon.assignment.domain.account.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.account.dto.AccountResponseDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    public static final long DEPOSIT_LIMIT = 3_000_000;

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final SavingAccountRepository savingAccountRepository;

    @Transactional
    public void createAccount(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        Account account = new Account(user);
        accountRepository.save(account);
    }

    @Transactional
    public void createSavingAccount(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        SavingAccount account = new SavingAccount(user);
        savingAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDto> getSavingAccounts(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        return savingAccountRepository.findByUser(user)
                .stream()
                .map(AccountResponseDto::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepositDto.Res depositToMainAccount(String email, long amount){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        // 메인 계좌를 가져오고 일일 한도를 넘지 않았다면 충전
        Account mainAccount = accountRepository.findByIdWithWriteLock(user.getMainAccountId())
                .orElseThrow(() -> new CustomException(ErrorCode.MAIN_ACCOUNT_PK_NOT_FOUND));
        if(mainAccount.getDailyTopUpAmount() + amount > DEPOSIT_LIMIT) throw new CustomException(ErrorCode.DEPOSIT_LIMIT_EXCEED);
        mainAccount.deposit(amount);

        return new DepositDto.Res(mainAccount.getBalance());
    }

    @Transactional
    public TransferDto.Res transferToSavings(String email, long savingId, long amount){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        // 메인 계좌를 가져오고 잔고가 이체할 양보다 많다면 출금
        Account mainAccount = accountRepository.findByIdWithWriteLock(user.getMainAccountId())
                .orElseThrow(() -> new CustomException(ErrorCode.MAIN_ACCOUNT_PK_NOT_FOUND));
        if (mainAccount.getBalance() < amount) throw new CustomException(ErrorCode.MAIN_ACCOUNT_BALANCE_EXCEED);
        mainAccount.withdraw(amount);

        // 적금 계좌를 가져오고 입금
        SavingAccount savingAccount = savingAccountRepository.findByIdWithWriteLock(savingId).orElseThrow();
        savingAccount.deposit(amount);

        return new TransferDto.Res(savingAccount.getBalance());
    }

}
