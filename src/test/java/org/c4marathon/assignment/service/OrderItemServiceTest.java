package org.c4marathon.assignment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.OrderItem;
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
class OrderItemServiceTest {

	private static Long customerId;

	@Autowired
	private OrderItemService orderItemService;

	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private ItemService itemService;

	@BeforeEach
	void beforeEach(){
		Member seller = new Member();
		seller.setUsername("판매자11");
		seller.setUserId("sellertest22");
		seller.setPassword("testtest");
		seller.setAddress(" 가정");
		seller.setPostalCode("122132");
		seller.setPhone("010434345545");
		memberService.register(seller, MemberType.ROLE_SELLER);

		Member customer = new Member();
		customer.setUsername("구매자11");
		customer.setUserId("customertest22");
		customer.setPassword("testtest");
		customer.setAddress("가정");
		customer.setPostalCode("122132");
		customer.setPhone("01033333333");
		Member register = memberService.register(customer, MemberType.ROLE_CUSTOMER);
		customerId = register.getMemberPk();

		Item item = new Item();
		item.setSeller(seller);
		item.setName("상품 테스트 1");
		item.setDescription("상품 설명 테스트");
		item.setStock(100);
		item.setPrice(10000);
		Item savedItem = itemService.saveItem(item, seller.getMemberPk());

		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setItemId(savedItem.getItemPk());
		cartItemDTO.setCount(5); // 장바구니에 5개 담기

		cartItemService.addCart(cartItemDTO, customer);
	}

	@Test
	void createOrderItems() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		List<OrderItem> orderItems = orderItemService.createOrderItems(allCartItem);

		Assertions.assertEquals(1, orderItems.size());
	}

	@Test
	void findOrderItemById() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		List<OrderItem> orderItems = orderItemService.createOrderItems(allCartItem);

		for (OrderItem orderItem : orderItems) {
			for (CartItem cartItem : allCartItem) {
				Assertions.assertEquals(orderItem.getItem(), cartItem.getItem());
				Assertions.assertEquals(orderItem.getCount(), cartItem.getCount());
			}
		}
	}
}