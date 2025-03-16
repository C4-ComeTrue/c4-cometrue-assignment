package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.global.util.Const.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transaction.domain.TransactionType.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.SavingProduct;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingProductRepository;
import org.c4marathon.assignment.account.dto.SavingAccountCreateRequest;
import org.c4marathon.assignment.account.dto.SavingAccountCreateResponse;
import org.c4marathon.assignment.account.exception.InsufficientBalanceException;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.account.exception.NotFoundSavingProductException;
import org.c4marathon.assignment.account.service.query.SavingAccountQueryService;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.c4marathon.assignment.global.util.AccountNumberUtil;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavingAccountService {
    private final SavingAccountQueryService savingAccountQueryService;
    private final AccountRepository accountRepository;
    private final SavingAccountRepository savingAccountRepository;
    private final SavingProductRepository savingProductRepository;
    private final TransactionRepository transactionRepository;

    private final MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);

    @Transactional
    public SavingAccountCreateResponse createSavingAccount(String mainAccountNumber, SavingAccountCreateRequest request) {

        Account account = accountRepository.findByAccountNumber(mainAccountNumber)
            .orElseThrow(NotFoundAccountException::new);

        SavingProduct savingProduct = savingProductRepository.findById(request.savingProductId())
            .orElseThrow(NotFoundSavingProductException::new);

        String savingAccountNumber = AccountNumberUtil.generateAccountNumber(SAVING_ACCOUNT_PREFIX);

        SavingAccount savingAccount = SavingAccount.create(
            savingAccountNumber,
            DEFAULT_BALANCE,
            request.depositAmount(),
            savingProduct,
            account.getAccountNumber()
        );

        savingAccountRepository.save(savingAccount);
        return new SavingAccountCreateResponse(savingAccount.getId());
    }

    /**
     * 정기적금 기능
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void depositFixedSavingAccount() {
        threadPoolExecutor.init();

        Long lastId = null;

        while (true) {
            List<SavingAccount> fixedSavingAccounts = savingAccountQueryService.findSavingAccountByFixedWithLastId(
                lastId, PAGE_SIZE);

            if (fixedSavingAccounts == null || fixedSavingAccounts.isEmpty()) {
                break;
            }
            for (SavingAccount fixedSavingAccount : fixedSavingAccounts) {
                threadPoolExecutor.execute(() -> processDepositSavingAccount(fixedSavingAccount));
            }
            lastId = fixedSavingAccounts.get(fixedSavingAccounts.size() - 1).getId();
        }
        threadPoolExecutor.waitToEnd();
    }

    /**
     * 이자 입금 기능
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void depositInterest() {
        threadPoolExecutor.init();

        Long lastId = null;

        while (true) {
            List<SavingAccount> savingAccounts = savingAccountQueryService.findAllSavingAccountByLastId(
                lastId, PAGE_SIZE);

            if (savingAccounts == null || savingAccounts.isEmpty()) {
                break;
            }
            for (SavingAccount savingAccount : savingAccounts) {
                threadPoolExecutor.execute(() -> processDepositInterest(savingAccount));
            }
            lastId = savingAccounts.get(savingAccounts.size() - 1).getId();
        }
        threadPoolExecutor.waitToEnd();
    }

    private void processDepositInterest(SavingAccount savingAccount) {
        long interest = savingAccount.calculateInterest();

        Account account = accountRepository.findByAccountNumberWithLock(savingAccount.getMainAccountNumber())
            .orElseThrow(NotFoundAccountException::new);

        account.deposit(interest);

        Transaction transaction = Transaction.create(
            INTEREST_ACCOUNT,
            account.getAccountNumber(),
            interest,
            IMMEDIATE_TRANSFER,
            SUCCESS_DEPOSIT,
            LocalDateTime.now()
        );
        transactionRepository.save(transaction);
    }

    private void processDepositSavingAccount(SavingAccount fixedSavingAccount) {
        Account account = accountRepository.findByAccountNumberWithLock(fixedSavingAccount.getMainAccountNumber())
            .orElseThrow(NotFoundAccountException::new);

        long depositAmount = fixedSavingAccount.getDepositAmount();

        if (!account.isSend(depositAmount)) {
            throw new InsufficientBalanceException();
        }
        account.withdraw(depositAmount);
        fixedSavingAccount.deposit(depositAmount);

        Transaction transaction = Transaction.create(
            account.getAccountNumber(),
            fixedSavingAccount.getSavingAccountNumber(),
            depositAmount,
            IMMEDIATE_TRANSFER,
            SUCCESS_DEPOSIT,
            LocalDateTime.now()
        );

        transactionRepository.save(transaction);
    }

}
