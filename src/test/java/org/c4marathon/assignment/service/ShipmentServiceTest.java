package org.c4marathon.assignment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.Sales;
import org.c4marathon.assignment.domain.Shipment;
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
class ShipmentServiceTest {

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

	@Autowired
	private ShipmentService shipmentService;

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

		List<CartItem> allCartItem = cartItemService.getAllCartItem(customer);
		Order proceeded = orderService.proceed(allCartItem, customerId, sellerId);
		orderedId = proceeded.getOrderPk();
	}

	@Test
	void issueTracker() {
		Shipment shipment = shipmentService.issueTracker(orderedId, "xxxxxxxx");

		Order order = orderService.findById(orderedId);

		assertFalse(order.isRefundable());
		assertNotNull(shipment.getRegisteredDate());
		assertEquals(order, shipment.getOrder());
		assertEquals("xxxxxxxx", shipment.getTrackingNumber());
		assertEquals(ShipmentStatus.DISPATCHED, order.getShipmentStatus());
	}

	@Test
	void completion() {
		Shipment shipment = shipmentService.issueTracker(orderedId, "xxxxxxxx");

		Order order = orderService.findById(orderedId);


		shipmentService.completion(orderedId);

		assertEquals(ShipmentStatus.COMPLETED, order.getShipmentStatus());
		assertNotNull(shipment.getCompletedDate());
	}
}
