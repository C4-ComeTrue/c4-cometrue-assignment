package org.c4marathon.assignment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.service.dto.CartItemDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class CartItemServiceTest {

	private static Long sellerId;
	private static Long customerId;

	private static Long itemId;

	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private ItemService itemService;

	@BeforeEach
	void beforeEach() {
		Member seller1 = new Member();
		seller1.setUserId("noogler02");
		seller1.setPostalCode("129-03");
		seller1.setValid(true);
		seller1.setPassword("test2");
		seller1.setAddress("경기도 남양주시 경춘로");
		seller1.setPhone("010-4822-2020");
		seller1.setUsername("홍길동");
		Member register = memberService.register(seller1, MemberType.ROLE_SELLER);
		sellerId = register.getMemberPk();

		Member customer1 = new Member();
		customer1.setUserId("noogler");
		customer1.setPostalCode("129-03");
		customer1.setValid(true);
		customer1.setPassword("test2");
		customer1.setAddress("경기도 남양주시 경춘로");
		customer1.setPhone("010-4822-2020");
		customer1.setUsername("홍길동");
		Member register2 = memberService.register(customer1, MemberType.ROLE_CUSTOMER);
		customerId = register2.getMemberPk();

		Item item = new Item();
		item.setName("테스트 상품");
		item.setDescription("테스트 설명");
		item.setStock(20);
		item.setPrice(20000);

		Item savedItem = itemService.saveItem(item, sellerId);
		itemId = savedItem.getItemPk();
	}

	@Test
	void addCart() {
		Member customer = memberService.findCustomerById(customerId);

		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setItemId(itemId);
		cartItemDTO.setCount(5); // 장바구니에 5개 담기

		CartItem cartItem = cartItemService.addCart(cartItemDTO, customer);
		CartItem findCartItem = cartItemService.findCartItemById(cartItem.getShoppingCartId(), customer);
		Assertions.assertEquals(cartItem, findCartItem);
	}

	@Test
	void removeCart() {
		Member customer = memberService.findCustomerById(customerId);

		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setItemId(itemId);
		cartItemDTO.setCount(5); // 장바구니에 5개 담기

		CartItem cartItem = cartItemService.addCart(cartItemDTO, customer);
		cartItemService.removeCart(cartItem.getShoppingCartId(), customer);

		RuntimeException runtimeException = assertThrows(RuntimeException.class, ()
			-> cartItemService.findCartItemById(cartItem.getShoppingCartId(), customer));
		Assertions.assertEquals(runtimeException.getMessage(),
			"NO_SUCH_ITEM : Item Not Found - 해당 제품이 장바구니에 없습니다.");
	}

	@Test
	void getAllCartItem(){
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Assertions.assertEquals(allCartItem.size(), 0);
	}

}
