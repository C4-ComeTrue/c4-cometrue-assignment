package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	@Query("select m from Member m where m.userType = :userType")
	List<Member> findByUserType(@Param(value = "userType") UserType userType);
}
