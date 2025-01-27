package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.MainAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MainAccountRepository extends JpaRepository<MainAccount, Long> {
}
