package org.c4marathon.assignment.transactional.domain.repository;

import org.c4marathon.assignment.transactional.domain.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionalRepository extends JpaRepository<Transactional, Long> {

}
