package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Boolean existsByUserId(String userId);

	@Query("select m from Member m where m.memberType = :memberType")
	List<Member> findByUserType(@Param(value = "memberType") MemberType memberType);
}
