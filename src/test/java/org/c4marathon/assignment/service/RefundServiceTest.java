package org.c4marathon.assignment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderStatus;
import org.c4marathon.assignment.domain.Payment;
import org.c4marathon.assignment.domain.Refund;
import org.c4marathon.assignment.service.dto.CartItemDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@Rollback
class RefundServiceTest {

	private static Long sellerId;
	private static Long customerId;

	private static Long itemId;

	@Autowired
	private MemberService memberService;

	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ItemService itemService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private RefundService refundService;

	@BeforeEach
	void beforeEach(){
		Member seller1 = new Member();
		seller1.setUserId("noogler0222");
		seller1.setPostalCode("129-03");
		seller1.setValid(true);
		seller1.setPassword("test2");
		seller1.setAddress("경기도 남양주시 경춘로");
		seller1.setPhone("010-4822-2020");
		seller1.setUsername("홍길동");
		Member seller = memberService.register(seller1, MemberType.ROLE_SELLER);
		sellerId = seller.getMemberPk();

		Member customer1 = new Member();
		customer1.setUserId("noogler2222");
		customer1.setPostalCode("129-03");
		customer1.setValid(true);
		customer1.setPassword("test2");
		customer1.setAddress("경기도 남양주시 경춘로");
		customer1.setPhone("010-4822-2020");
		customer1.setUsername("홍길동");
		Member customer = memberService.register(customer1, MemberType.ROLE_CUSTOMER);
		customerId = customer.getMemberPk();

		Member admin = new Member();
		admin.setUserId("noogler222255");
		admin.setPostalCode("129-03");
		admin.setValid(true);
		admin.setPassword("test2");
		admin.setAddress("경기도 남양주시 경춘로");
		admin.setPhone("010-4822-2020");
		admin.setUsername("홍길동");
		memberService.register(admin, MemberType.ROLE_FINANCE_ADMIN);

		paymentService.charge(20000000, customer);

		Item item = new Item();
		item.setName("상품 테스트");
		item.setDescription("테스트 상품 설명");
		item.setStock(200);
		item.setPrice(20000);
		item.setSeller(seller);
		Item savedItem = itemService.saveItem(item, sellerId);
		itemId = savedItem.getItemPk();

		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setItemId(savedItem.getItemPk());
		cartItemDTO.setCount(5); // 장바구니에 5개 담기

		cartItemService.addCart(cartItemDTO, customer);

	}


	@Test
	void refund() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Item previousItem = itemService.findById(itemId);
		Integer previousPurchaseStock = previousItem.getStock();
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);
		Item updatedItem = itemService.findById(itemId);

		Refund refund = refundService.refund(customerId, proceeded.getOrderPk());

		Item updatedRefundedItem = itemService.findById(itemId);
		Integer refundedPurchaseStock = updatedRefundedItem.getStock();

		assertEquals(refundedPurchaseStock, previousPurchaseStock);
		Payment payment = refund.getPayment();
		assertEquals(payment.getValue(), updatedItem.getPrice() * 5);
		assertEquals(ChargeType.CHARGE, payment.getValueType()); // 고객에 반품된 내역이 조회됨.
	}

	@Test
	void refundOrderStatusException() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Item previousItem = itemService.findById(itemId);
		itemService.updateItem(previousItem, previousItem.getItemPk(), sellerId);
		Integer previousPurchaseStock = previousItem.getStock();
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);
		proceeded.setOrderStatus(OrderStatus.ORDERED_SHIPPED);

		RuntimeException runtimeException = assertThrows(RuntimeException.class,
			() -> refundService.refund(customerId, proceeded.getOrderPk()));
		Assertions.assertEquals("INVALID_ARGUMENT : Invalid Argument - 배송이 이미 시작되어 반품이 어렵습니다", runtimeException.getMessage());
	}

	@Test
	void findById() {
	}

	@Test
	void findBySeller() {
	}
}
