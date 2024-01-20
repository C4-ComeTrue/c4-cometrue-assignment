package org.c4marathon.assignment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.Sales;
import org.c4marathon.assignment.service.dto.CartItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@Rollback
class SalesServiceTest {

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
	private OrderItemService orderItemService;

	@Autowired
	private SalesService salesService;

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
	void getSalesBySeller() {
		Member seller = memberService.findSellerById(sellerId);
		List<Sales> salesBySeller = salesService.findSalesBySeller(seller);

		assertEquals(0, salesBySeller.size());
		Member customer = memberService.findCustomerById(customerId);
		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);
		orderService.orderConfirmation(customerId, proceeded.getOrderPk());
		salesBySeller = salesService.findSalesBySeller(seller);
		for (Sales sales : salesBySeller) {
			System.out.println("sales.toString() = " + sales.toString());
		}
		assertEquals(1, salesBySeller.size());
	}

	@Test
	void getSalesBySellerException() {
		Member customer = memberService.findCustomerById(customerId);

		RuntimeException runtimeException = assertThrows(RuntimeException.class,
			() -> salesService.findSalesBySeller(customer));
		assertEquals("NO_PERMISSION : No Permission - 판매자만 접근할 수 있는 기능입니다", runtimeException.getMessage());
	}

	@Test
	void addSales() {
		Member seller = memberService.findSellerById(sellerId);
		Member customer = memberService.findCustomerById(customerId);

		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		List<OrderItem> orderItems = orderItemService.createOrderItems(allCartItem);
		OrderItem orderItem = orderItems.get(0);

		salesService.addSales(orderItem, customer, seller, 2000, ChargeType.CHARGE);

		List<Sales> salesList = salesService.findSalesBySeller(seller);
		Sales sales = salesList.get(0);
		assertNotNull(sales.getSalesPk());
		assertEquals(2000, sales.getValue());
		assertEquals(customer, sales.getSender());
		assertEquals(seller, sales.getReceiver());
		assertEquals(orderItem, sales.getOrderItem());
		assertEquals(ChargeType.CHARGE, sales.getChargeType());
	}

	@Test
	void findSalesByOrderItem() {
		Member seller = memberService.findSellerById(sellerId);
		Member customer = memberService.findCustomerById(customerId);

		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		List<OrderItem> orderItems = orderItemService.createOrderItems(allCartItem);
		OrderItem orderItem = orderItems.get(0);

		salesService.addSales(orderItem, customer, seller, 2000, ChargeType.CHARGE);

		List<Sales> salesByOrderItem = salesService.findSalesByOrderItem(orderItem);
		assertEquals(1, salesByOrderItem.size());
	}
}