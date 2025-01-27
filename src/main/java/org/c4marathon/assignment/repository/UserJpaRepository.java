package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);
}
