package org.c4marathon.assignment.service;

import org.c4marathon.assignment.dto.request.PostMainAccountReq;
import org.c4marathon.assignment.dto.request.PostSavingsAccountReq;
import org.c4marathon.assignment.dto.request.WithdrawMainAccountReq;
import org.c4marathon.assignment.dto.response.MainAccountInfoRes;
import org.c4marathon.assignment.dto.response.WithdrawInfoRes;
import org.c4marathon.assignment.entity.Account;
import org.c4marathon.assignment.entity.SavingsAccount;
import org.c4marathon.assignment.entity.User;
import org.c4marathon.assignment.exception.CustomException;
import org.c4marathon.assignment.exception.ErrorCode;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.SavingsAccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final SavingsAccountRepository savingsAccountRepository;
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;

	public void createSavingsAccount(PostSavingsAccountReq postSavingsAccountReq) {
		User user = userRepository.findByEmail(postSavingsAccountReq.email())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

		savingsAccountRepository.save(new SavingsAccount(user.getId()));
	}

	@Transactional
	public MainAccountInfoRes depositMainAccount(PostMainAccountReq postMainAccountReq) {
		User user = userRepository.findById(postMainAccountReq.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ID));

		Account account = accountRepository.findByIdWithWriteLock(user.getMainAccount())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_MAIN_ACCOUNT));

		charge(postMainAccountReq.amount(), account);

		return new MainAccountInfoRes(account);
	}

	public void charge(long amount, Account account) {
		if (account.isDailyLimitExceeded(amount)) {
			throw new CustomException(ErrorCode.EXCEEDED_DEPOSIT_LIMIT);
		}
		account.deposit(amount);
	}

	@Transactional
	public WithdrawInfoRes withdrawForSavings(WithdrawMainAccountReq withdrawMainAccountReq) {
		User user = userRepository.findById(withdrawMainAccountReq.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ID));

		Account account = accountRepository.findByIdWithWriteLock(user.getMainAccount())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_MAIN_ACCOUNT));

		if (account.isBalanceInsufficient(withdrawMainAccountReq.amount())) {
			throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
		}

		SavingsAccount savingsAccount = savingsAccountRepository.findByIdAndUserId(
				withdrawMainAccountReq.savingsAccount(),
				withdrawMainAccountReq.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_SAVINGS_ACCOUNT));

		account.withdraw(withdrawMainAccountReq.amount());
		savingsAccount.deposit(withdrawMainAccountReq.amount());

		return new WithdrawInfoRes(account.getBalance(), savingsAccount.getBalance());
	}
}
