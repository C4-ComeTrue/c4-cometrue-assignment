package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
