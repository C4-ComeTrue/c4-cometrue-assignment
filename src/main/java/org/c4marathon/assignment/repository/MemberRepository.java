package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
