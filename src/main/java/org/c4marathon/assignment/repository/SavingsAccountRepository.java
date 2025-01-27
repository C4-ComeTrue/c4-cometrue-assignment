package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.entity.SavingsAccount;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SavingsAccountRepository {
	private final SavingsAccountJpaRepository savingsAccountJpaRepository;

	public void save(SavingsAccount savingsAccount) {
		savingsAccountJpaRepository.save(savingsAccount);
	}
}
