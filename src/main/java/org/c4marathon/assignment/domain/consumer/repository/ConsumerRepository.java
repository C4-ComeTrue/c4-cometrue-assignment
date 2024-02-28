package org.c4marathon.assignment.domain.consumer.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumerRepository extends JpaRepository<Consumer, Long> {

	Boolean existsByEmail(String email);

	Optional<Consumer> findByEmail(String email);
}
