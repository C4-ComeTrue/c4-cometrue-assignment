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
import org.c4marathon.assignment.domain.ShipmentStatus;
import org.c4marathon.assignment.service.dto.CartItemDTO;
import org.junit.jupiter.api.Assertions;
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
class OrderServiceTest {

	private static Long sellerId;
	private static Long customerId;

	private static Long itemId;


	@Autowired
	private OrderService orderService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private ItemService itemService;

	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private MonetaryTransactionService monetaryTransactionService;

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
		itemId = savedItem.getItemPk();

		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setItemId(savedItem.getItemPk());
		cartItemDTO.setCount(5); // 장바구니에 5개 담기

		cartItemService.addCart(cartItemDTO, customer);

	}

	@Test
	@DisplayName("ID를 통해 복합주문을 검색할 수 있다.")
	void findById() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);

		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);

		Order order = orderService.findById(proceeded.getOrderPk());

		Assertions.assertEquals(order, proceeded);
	}

	@Test
	@DisplayName("복합 주문이 진행되면 단건 주문 구매 정보는 sales 에 종합 주문 금")
	void proceed() {
		Member customer = memberService.findCustomerById(customerId);
		Member seller = memberService.findSellerById(sellerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Item previousItem = itemService.findById(itemId);
		Integer previousStock = previousItem.getStock();
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);
		Item updatedItem = itemService.findById(itemId);
		Integer updatedStock = updatedItem.getStock();
		// 결제 후 재고 변화 확인
		assertEquals(5, previousStock - updatedStock);
		assertEquals(ShipmentStatus.PENDING, proceeded.getShipmentStatus());
		assertEquals(OrderStatus.ORDERED_PENDING, proceeded.getOrderStatus());
		assertEquals(customer, proceeded.getCustomer());
		assertEquals(proceeded.getSeller(), seller);

		// 결제 금액 산정 값 확인
		Payment payment = proceeded.getPayment();
		assertEquals(payment.getValue(), updatedItem.getPrice() * 5);
		assertEquals(ChargeType.DISCHARGE, payment.getValueType());

		// Order 테이블 기입되었는지 확인.
		Order findOrder = orderService.findById(proceeded.getOrderPk());
		Assertions.assertEquals(findOrder, proceeded);
	}

	@Test
	void requestRefund() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);
		orderService.requestRefund(proceeded.getOrderPk(), customerId);

		assertEquals(OrderStatus.REFUND_REQUESTED_BY_CUSTOMER, proceeded.getOrderStatus());
		assertFalse(proceeded.isRefundable());
		assertEquals(ShipmentStatus.REFUND_PENDING, proceeded.getShipmentStatus());
	}

	@Test
	void orderConfirmation() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);

		orderService.orderConfirmation(customerId, proceeded.getOrderPk());
		assertEquals(OrderStatus.CUSTOMER_ACCEPTED, proceeded.getOrderStatus());
	}

	@Test
	void requestRefundNoPermissionException() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);

		Member customer1 = new Member();
		customer1.setUserId("noog");
		customer1.setPostalCode("129-03");
		customer1.setValid(true);
		customer1.setPassword("test2");
		customer1.setAddress("경기도 남양주시 경춘로");
		customer1.setPhone("010-4822-2020");
		customer1.setUsername("홍길동");
		Member otherCustomer = memberService.register(customer1, MemberType.ROLE_CUSTOMER);

		Long orderKey = proceeded.getOrderPk();
		Long otherCustomerKey = otherCustomer.getMemberPk();
		RuntimeException runtimeException = assertThrows(RuntimeException.class,
			() -> orderService.requestRefund(orderKey, otherCustomerKey));

		assertEquals("NO_PERMISSION : No Permission - 다른 사용자가 구입한 요청에 대한 반품 요청입니다.", runtimeException.getMessage());
	}

	@Test
	void requestRefundInvalidArgumentException() {
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);

		orderService.orderConfirmation(customerId, proceeded.getOrderPk());

		Long orderedPk = proceeded.getOrderPk();

		RuntimeException runtimeException = assertThrows(RuntimeException.class,
			() -> orderService.requestRefund(orderedPk, customerId));
		assertEquals("INVALID_ARGUMENT : Invalid Argument - 배송 대기중인 상태에서만 반품 신청이 가능합니다.",
			runtimeException.getMessage());
	}


}
