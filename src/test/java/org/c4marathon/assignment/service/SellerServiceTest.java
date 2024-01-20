package org.c4marathon.assignment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.OrderStatus;
import org.c4marathon.assignment.domain.Sales;
import org.c4marathon.assignment.domain.ShipmentStatus;
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
class SellerServiceTest {

	private static Long sellerId;
	private static Long customerId;

	private static Long orderedId;

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

	@Autowired
	private SalesService salesService;

	@Autowired
	private SellerService sellerService;

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

		List<Sales> salesBySeller = salesService.getSalesBySeller(seller);

		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);
		orderedId = proceeded.getOrderPk();
	}

	@Test
	void findOrders() {
		List<OrderItem> orders = sellerService.findOrders(itemId, sellerId);
		assertEquals(1, orders.size());
	}

	@Test
	void findOrderByOrderStatus() {
		List<Order> ordersByOrderStatus = sellerService.findOrdersByOrderStatus(sellerId, OrderStatus.ORDERED_PENDING);
		assertEquals(1, ordersByOrderStatus.size());
	}

	@Test
	void acceptOrder() {
		Order pendingOrder  = orderService.findById(orderedId);
		assertEquals(OrderStatus.ORDERED_PENDING, pendingOrder.getOrderStatus());
		Order orderAccepted = sellerService.acceptOrder(orderedId, sellerId);
		assertEquals(OrderStatus.ORDERED_ACCEPTED, orderAccepted.getOrderStatus());
	}

	@Test
	void issueShipment() {
		sellerService.issueShipment(orderedId, sellerId, "2002000000");
		Order order = orderService.findById(orderedId);
		assertNotNull(order.getShipment());
		assertEquals(ShipmentStatus.DISPATCHED, order.getShipmentStatus());
	}

	@Test
	void shipmentCompleted() {
		Order order = orderService.findById(orderedId);
		sellerService.issueShipment(orderedId, sellerId, "2002000000");
		sellerService.shipmentCompleted(orderedId, sellerId);
		assertEquals(ShipmentStatus.COMPLETED, order.getShipmentStatus());

	}
}
