package org.c4marathon.assignment.domain.pay.repository;

import org.c4marathon.assignment.domain.pay.entity.Pay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayRepository extends JpaRepository<Pay, Long> {
}
