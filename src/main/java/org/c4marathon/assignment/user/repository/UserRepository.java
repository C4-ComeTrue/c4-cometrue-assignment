package org.c4marathon.assignment.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUserId(String userId);
}
