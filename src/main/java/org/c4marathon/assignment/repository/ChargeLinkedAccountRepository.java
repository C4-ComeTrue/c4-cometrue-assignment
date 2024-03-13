package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.entity.ChargeLinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeLinkedAccountRepository extends JpaRepository<ChargeLinkedAccount, Long> {

	Optional<ChargeLinkedAccount> findByAccountIdAndMain(long accountId, boolean main);

}
