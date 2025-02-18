package org.c4marathon.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	boolean existsById(Long userId);

	@Query("SELECT COUNT(u) FROM User u WHERE u.id IN :ids")
	int countByIds(@Param("ids") List<Long> ids);
}
