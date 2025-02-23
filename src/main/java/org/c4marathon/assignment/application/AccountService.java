package org.c4marathon.assignment.application;

import static org.c4marathon.assignment.global.AccountUtils.*;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.type.AccountType;
import org.c4marathon.assignment.domain.UserRepository;
import org.c4marathon.assignment.domain.dto.response.CreatedAccountInfo;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	/**
	 * 계좌 생성. accountType은 생성 계좌가 적금 계좌인지 입출금 계좌인지를 판단.
	 * @param userId
	 * @param accountType
	 * @return
	 */
	public CreatedAccountInfo create(long userId, AccountType accountType) {
		String accountNumber = getAccountNumber();

		userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found."));

		Account newAccount = Account.builder().userId(userId).accountType(accountType).accountNumber(accountNumber).build();
		Account savedAccount = accountRepository.save(newAccount);

		return new CreatedAccountInfo(savedAccount.getAccountNumber(), savedAccount.getCreatedAt(),
			savedAccount.getAccountType(), savedAccount.getBalance(), savedAccount.isMain());
	}

}
