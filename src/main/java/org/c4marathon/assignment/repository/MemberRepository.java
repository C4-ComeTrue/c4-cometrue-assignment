package org.c4marathon.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	@Query("select m from Member m where m.userId = :userId")
	Optional<Member> findMemberByUserId(@Param("userId") String userId);

	boolean existsByUserId(String userId);

	@Query("select m from Member m where m.memberType = :memberType")
	List<Member> findByUserType(@Param(value = "memberType") MemberType memberType);

	@Query("select m from Member m where m.memberType = 'ROLE_FINANCE_ADMIN'")
	Member findFinancialAdmin();

	@Query("select m from Member m where m.memberType = 'ROLE_CUSTOMER' and m.memberPk = :customerId")
	Optional<Member> findCustomerById(@Param("customerId") Long customerId);

	@Query("select m from Member m where m.memberType = 'ROLE_SELLER' and m.memberPk = :sellerId")
	Optional<Member> findSellerById(@Param("sellerId") Long sellerId);

}
