package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    Long random = new Random().nextLong();

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean chargeMainAccount(ChargeDto chargeDto) {

        Optional<Account> optionalAccount = accountRepository.findByAccountNum(chargeDto.accountNum());

        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();

            account.setAmount(chargeDto.chargeMoney());

            accountRepository.save(account);
            return true;
        }

        return false;
    }

    public boolean craeteSavingAccount(Long userId){
        Optional<User> userOptional  = userRepository.findByUserId(String.valueOf(userId));

        if (userOptional .isPresent()){
            User user = userOptional.get();
            Account account = Account.builder()
                    .accountNum(random)
                    .type(AccountType.SAVING_ACCOUNT)
                    .accountPw(1234)
                    .amount(0)
                    .user(user)
                    .build();

            accountRepository.save(account);
            return true;
        }else {
            return false;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean sendSavingAccount(Long userId,SendDto sendDto){
        Optional<Account> optionalAccount = accountRepository.findByAccountNum(sendDto.accountNum());
        Optional<Account> optionalAccountOther = accountRepository.findByAccountPw(sendDto.accountPw());
        Optional<Account> mainAccount = accountRepository.findByUser_IdAndType(userId, AccountType.MAIN_ACCOUNT);

        if (optionalAccount.isPresent() && optionalAccountOther.isPresent()){

            Account main = mainAccount.get();
            Account saving = optionalAccount.get();
            int checkMoney = main.getAmount() - sendDto.sendMoney();

            if (checkMoney > 0){

                main.minusAmount(sendDto.sendMoney());
                saving.plusAmount(sendDto.sendMoney());
                accountRepository.save(main);
                accountRepository.save(saving);
                return true;

            }else {
                throw new NullPointerException();
            }
        }else {
            return false;
        }
    }
}
