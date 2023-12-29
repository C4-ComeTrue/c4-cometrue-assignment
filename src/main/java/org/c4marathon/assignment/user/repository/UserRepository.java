package org.c4marathon.assignment.user.repository;

import org.c4marathon.assignment.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
