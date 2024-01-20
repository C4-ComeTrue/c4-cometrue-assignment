package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

	@Query("select i from Item i where i.seller = :member")
	List<Item> findAllByMemberId(@Param("member") Member member);

	@Query("select i from Item i join fetch OrderItem o where i.seller.memberPk = :memberId")
	List<Item> findAllByMemberIdForOrderItems(@Param("memberId") Long memberId);
}
