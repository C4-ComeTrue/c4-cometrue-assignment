package org.c4marathon.assignment.repository;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.entity.User;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository {
	private final UserJpaRepository userJpaRepository;

	public boolean isEmailExist(String email) {
		return userJpaRepository.existsByEmail(email);
	}

	public User save(User user) {
		return userJpaRepository.save(user);
	}
}
