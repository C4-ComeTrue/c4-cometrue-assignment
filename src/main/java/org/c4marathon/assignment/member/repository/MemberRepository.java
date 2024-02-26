package org.c4marathon.assignment.member.repository;

import org.c4marathon.assignment.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Member findMemberByMemberId(String memberId);
}
