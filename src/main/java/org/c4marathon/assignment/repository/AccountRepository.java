package org.c4marathon.assignment.repository;

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
}
