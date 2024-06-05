package org.c4marathon.assignment.accounts.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.accounts.entity.Account;
import org.c4marathon.assignment.accounts.entity.AccountType;
import org.c4marathon.assignment.accounts.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl {
    private final AccountRepository accountRepository;

    public Account addAcount(String id , AccountType type) {
        Account account = new Account(0,id,0,3000000,type);
        return accountRepository.save(account);
    }

    @Transactional
    public Account chargeBalance(Map<String, String> map) throws Exception {

        Account account = accountRepository.findById(map.get("account"))
                .orElseThrow(() -> new Exception("계좌없음"));

        account.setBalance(account.getBalance() + Integer.parseInt(map.get("amount")));

        return account;
    }

    @Transactional
    public Account transfer(Map<String, String> map) throws Exception {

        long amount = Long.parseLong(map.get("amount"));

        Account sender = accountRepository.findById(map.get("sender"))
                .orElseThrow(() -> new Exception("본인 계좌 없음"));

        if(sender.getBalance() < amount) {
            throw new Exception("잔액 부족");
        }

        if(sender.getTransferLimit() < amount) {
            throw new Exception("이체 한도 초과");
        }

        Account receiver =  accountRepository.findById(map.get("receiver"))
                .orElseThrow(() -> new Exception("상대 계좌 없음"));

        sender.setBalance(sender.getBalance() - amount);
        sender.setTransferLimit(sender.getTransferLimit() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        return receiver;
    }
}
