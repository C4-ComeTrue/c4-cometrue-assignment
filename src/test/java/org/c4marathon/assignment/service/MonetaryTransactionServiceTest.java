package org.c4marathon.assignment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.service.dto.CartItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class MonetaryTransactionServiceTest {


	private static Long sellerId;

	private static Long customerId;

	@Autowired
	private MemberService memberService;

	@Autowired
	private ItemService itemService;

	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private OrderItemService orderItemService;

	@Autowired
	private PaymentService paymentService;

	@BeforeEach
	void beforeEach(){
		Member seller1 = new Member();
		seller1.setUserId("noogler02");
		seller1.setPostalCode("129-03");
		seller1.setValid(true);
		seller1.setPassword("test2");
		seller1.setAddress("경기도 남양주시 경춘로");
		seller1.setPhone("010-4822-2020");
		seller1.setUsername("홍길동");
		Member seller = memberService.register(seller1, MemberType.ROLE_SELLER);
		sellerId = seller.getMemberPk();

		Member customer1 = new Member();
		customer1.setUserId("noogler");
		customer1.setPostalCode("129-03");
		customer1.setValid(true);
		customer1.setPassword("test2");
		customer1.setAddress("경기도 남양주시 경춘로");
		customer1.setPhone("010-4822-2020");
		customer1.setUsername("홍길동");
		Member customer = memberService.register(customer1, MemberType.ROLE_CUSTOMER);
		customerId = customer.getMemberPk();

		paymentService.charge(20000000, customer);

		Item item = new Item();
		item.setName("상품 테스트");
		item.setDescription("테스트 상품 설명");
		item.setStock(200);
		item.setPrice(20000);
		item.setSeller(seller);
		Item savedItem = itemService.saveItem(item, sellerId);

		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setItemId(savedItem.getItemPk());
		cartItemDTO.setCount(5); // 장바구니에 5개 담기

		cartItemService.addCart(cartItemDTO, customer);

		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		List<OrderItem> orderItems = orderItemService.createOrderItems(allCartItem);

	}

	@Test
	@DisplayName("제품 구매, 고객에 의해 확정시 5% 수수료를 떼고 판매자에게 지급")
	void commissionProcedure() {

	}

	@Test
	@DisplayName("제품 구매시 고객의 Payment -> 본사 Sales 로 금액 이동")
	void transactionsForSelling() {
	}

	@Test
	@DisplayName("제품 환불 요청시 본사 Sales -> 고객 Payment 로 금액 이동")
	void transactionsForRefunding() {
	}
}
