package org.c4marathon.assignment.service;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.repository.ItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@Rollback
class ItemServiceTest {

	private static Long sellerId1;

	private static Long sellerId2;

	private static Long customerId;

	@Autowired
	private ItemService itemService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private ItemRepository itemRepository;

	@PersistenceContext
	private EntityManager em;

	@BeforeEach
	void createMember() {
		Member seller1 = new Member();
		seller1.setUserId("noogler02");
		seller1.setPostalCode("129-03");
		seller1.setValid(true);
		seller1.setPassword("test2");
		seller1.setAddress("경기도 남양주시 경춘로");
		seller1.setPhone("010-4822-2020");
		seller1.setUsername("홍길동");
		Member register = memberService.register(seller1, MemberType.ROLE_SELLER);
		sellerId1 = register.getMemberPk();

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

		Member seller2 = new Member();
		seller2.setUserId("noogl");
		seller2.setPostalCode("129-03");
		seller2.setValid(true);
		seller2.setPassword("test2");
		seller2.setAddress("경기도 남양주시 경춘로");
		seller2.setPhone("010-4822-2020");
		seller2.setUsername("홍길동");
		Member register3 = memberService.register(seller2, MemberType.ROLE_SELLER);
		sellerId2 = register3.getMemberPk();

	}

	@Test
	@DisplayName("판매자는 제품을 등록할 수 있다.")
	void saveItem() {
		Item item = new Item();
		item.setName("테스트 상품");
		item.setDescription("테스트 설명");
		item.setStock(20);
		item.setPrice(20000);

		Item savedItem = itemService.saveItem(item, sellerId1);

		Optional<Item> optionalItem = itemRepository.findById(savedItem.getItemPk());

		Assertions.assertEquals(optionalItem.get(), savedItem);
	}

	@Test
	@DisplayName("판매자만이 제품을 등록할 수 있다")
	void saveItemAuthenticityException() {
		Item item = new Item();
		item.setName("테스트 상품");
		item.setDescription("테스트 설명");
		item.setStock(20);
		item.setPrice(20000);

		Assertions.assertThrows(RuntimeException.class,
			() -> itemService.saveItem(item, customerId));
	}

	@Test
	@DisplayName("판매자는 제품 정보를 갱신할 수 있다.")
	void updateItem() {
		Item item = new Item();
		item.setName("테스트 상품");
		item.setDescription("테스트 설명");
		item.setStock(20);
		item.setPrice(20000);

		Item savedItem = itemService.saveItem(item, sellerId1);

		Item findItem = itemService.findById(savedItem.getItemPk());
		findItem.setDescription("테스트 설명 변경");

		Item updatedItem = itemService.updateItem(findItem, savedItem.getItemPk(), sellerId1);
		Assertions.assertEquals(updatedItem, findItem);
	}

	@Test
	@DisplayName("판매자만이 제품 정보를 갱신할 수 있다")
	void updateItemAuthenticityException() {
		Item item = new Item();
		item.setName("테스트 상품");
		item.setDescription("테스트 설명");
		item.setStock(20);
		item.setPrice(20000);

		Item savedItem = itemService.saveItem(item, sellerId1);

		Item findItem = itemService.findById(savedItem.getItemPk());
		findItem.setDescription("테스트 설명 변경");

		Long savedItemPk = savedItem.getItemPk();

		RuntimeException runtimeException = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
			() -> itemService.updateItem(findItem, savedItemPk, sellerId2));
		Assertions.assertEquals("NO_PERMISSION : No Permission - 잘못된 접근입니다", runtimeException.getMessage());
	}

	@Test
	@DisplayName("제품은 ID(PK)를 기준으로 조회될 수 있다. (READ_ONLY)")
	void findById() {
		Item item = new Item();
		item.setName("테스트 상품");
		item.setDescription("테스트 설명");
		item.setStock(20);
		item.setPrice(20000);

		Item savedItem = itemService.saveItem(item, sellerId1);

		Item findItem = itemService.findById(savedItem.getItemPk());
		Assertions.assertEquals(findItem, savedItem);
	}

	@Test
	@DisplayName("제품이 PK를 통해 조회되지 않는 경우 예외를 반환해야한다.")
	void findByIdNotFound() {
		RuntimeException runtimeException = org.junit.jupiter.api.Assertions.assertThrows(
			RuntimeException.class, () -> itemService.findById(9999999L)
		);
		Assertions.assertEquals("NO_SUCH_ITEM : Item Not Found - 상품을 찾을 수 없습니다"
			, runtimeException.getMessage());
	}

	@Test
	@DisplayName("제품은 판매자를 기준으로 조회될 수 있다. (READ_ONLY)")
	void findBySeller() {
		Item item = new Item();
		item.setName("테스트 상품");
		item.setDescription("테스트 설명");
		item.setStock(20);
		item.setPrice(20000);

		itemService.saveItem(item, sellerId1);

		Member seller = memberService.findSellerById(sellerId1);
		List<Item> items = itemService.findBySeller(seller);
		Assertions.assertEquals(1, items.size());
	}

	@Test
	@DisplayName("제품은 판매자를 기준으로 조회될 수 있고, 비어있는 경우에도 빈 리스트를 정상 반환한다.")
	void findBySellerEmpty() {
		Member seller = memberService.findSellerById(sellerId1);
		List<Item> items = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> itemService.findBySeller(seller));
		Assertions.assertEquals(0, items.size());

	}
}
