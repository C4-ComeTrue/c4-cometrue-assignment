package org.c4marathon.assignment.accounts.repository;

import org.c4marathon.assignment.accounts.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account,String> {

    List<Account> findAccountById(String id);
}
