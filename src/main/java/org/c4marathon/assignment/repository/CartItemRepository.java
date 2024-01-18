package org.c4marathon.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	@Query("select c from CartItem c where c.shoppingCartId = :cartItemId and c.member = :member")
	Optional<CartItem> findCartItemByIdAndMember(Long cartItemId, Member customer);

	@Query("select c from CartItem c where c.member = :customer")
	List<CartItem> findAllByCustomer(Member customer);

}
