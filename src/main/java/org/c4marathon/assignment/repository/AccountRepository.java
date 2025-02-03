package org.c4marathon.assignment.repository;

import java.time.Instant;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.entity.Account;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountRepository {
	private final AccountJpaRepository accountJpaRepository;

	public void save(Account account) {
		accountJpaRepository.save(account);
	}

	public Optional<Account> findByIdWithWriteLock(Long id) {
		return accountJpaRepository.findByIdWithWriteLock(id);
	}

	public boolean existsById(long receiverMainAccount) {
		return accountJpaRepository.existsById(receiverMainAccount);
	}

	public int updateBalance(long receiverMainAccount, long amount) {
		return accountJpaRepository.updateBalance(receiverMainAccount,amount);
	}

	public void initDailyChargedAmount(Instant now) {
		accountJpaRepository.initDailyChargedAmount(now);
	}
}
