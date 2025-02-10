package org.c4marathon.assignment.repository;

import java.util.List;
import java.util.Optional;

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

	public Optional<User> findByEmail(String email) {
		return userJpaRepository.findByEmail(email);
	}

	public Optional<User> findById(Long userId) {
		return userJpaRepository.findById(userId);
	}

	public int countByIds(List<Long> userIds) {
		return userJpaRepository.countByIds(userIds);
	}
}
