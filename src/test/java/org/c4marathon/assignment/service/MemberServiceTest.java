package org.c4marathon.assignment.service;

import java.util.List;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.junit.jupiter.api.Assertions;
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
class MemberServiceTest {

	@Autowired
	private MemberService memberService;

	@Test
	@DisplayName("모든 정보가 기입되었을 때 회원가입을 테스트")
	void register() {
		// Given
		Member member = new Member();
		member.setUserId("noogler0258");
		member.setPostalCode("129-01");
		member.setValid(true);
		member.setPassword("test1");
		member.setAddress("서울특별시 광진구 자양대로");
		member.setPhone("010-4832-2000");
		member.setUsername("김윤식");
		Member registeredMember = memberService.register(member, MemberType.ROLE_CUSTOMER);

		// When
		Member findMember = memberService.findCustomerById(member.getMemberPk());

		// Then
		org.assertj.core.api.Assertions.assertThat(findMember).isEqualTo(registeredMember);
	}

	@Test
	@DisplayName("동일한 아이디를 가진 사용자의 경우 예외처리")
	void duplicatedUserId() {
		Member member1 = new Member();
		member1.setUserId("noogler0258");
		member1.setPostalCode("129-01");
		member1.setValid(true);
		member1.setPassword("test1");
		member1.setAddress("서울특별시 광진구 자양대로");
		member1.setPhone("010-4832-2000");
		member1.setUsername("김윤식");
		Member registeredMember1 = memberService.register(member1, MemberType.ROLE_CUSTOMER);

		Member member2 = new Member();
		member2.setUserId("noogler0258");
		member2.setPostalCode("129-03");
		member2.setValid(true);
		member2.setPassword("test2");
		member2.setAddress("경기도 남양주시 경춘로");
		member2.setPhone("010-4822-2020");
		member2.setUsername("홍길동");

		Assertions.assertThrows(RuntimeException.class,
			()-> memberService.register(member2, MemberType.ROLE_CUSTOMER));
	}

	@Test
	@DisplayName("Customer 의 PK를 통해 조회할 수 있다 (소비자 한정)")
	void findByCustomerId() {
		Member member1 = new Member();
		member1.setUserId("noogler0258");
		member1.setPostalCode("129-01");
		member1.setValid(true);
		member1.setPassword("test1");
		member1.setAddress("서울특별시 광진구 자양대로");
		member1.setPhone("010-4832-2000");
		member1.setUsername("김윤식");
		Member registeredMember1 = memberService.register(member1, MemberType.ROLE_CUSTOMER);
		Long memberPk = registeredMember1.getMemberPk();


		Member customer = memberService.findCustomerById(memberPk);
		org.assertj.core.api.Assertions.assertThat(customer).isEqualTo(registeredMember1);
	}

	@Test
	@DisplayName("Seller의 PK를 Customer 의 PK 검색 메서드로 조회할 수 없다")
	void findByCustomerException() {
		Member member1 = new Member();
		member1.setUserId("noogler0258");
		member1.setPostalCode("129-01");
		member1.setValid(true);
		member1.setPassword("test1");
		member1.setAddress("서울특별시 광진구 자양대로");
		member1.setPhone("010-4832-2000");
		member1.setUsername("김윤식");
		Member registeredMember1 = memberService.register(member1, MemberType.ROLE_SELLER);
		Long memberPk = registeredMember1.getMemberPk();


		Assertions.assertThrows(RuntimeException.class, () -> memberService.findCustomerById(memberPk));
	}

	@Test
	@DisplayName("Seller 의 PK를 통해 조회할 수 있다 (판매자 한정)")
	void findBySellerId() {
		Member member1 = new Member();
		member1.setUserId("noogler0258");
		member1.setPostalCode("129-01");
		member1.setValid(true);
		member1.setPassword("test1");
		member1.setAddress("서울특별시 광진구 자양대로");
		member1.setPhone("010-4832-2000");
		member1.setUsername("김윤식");
		Member registeredMember1 = memberService.register(member1, MemberType.ROLE_SELLER);
		Long memberPk = registeredMember1.getMemberPk();


		Member seller = memberService.findSellerById(memberPk);
		org.assertj.core.api.Assertions.assertThat(seller).isEqualTo(registeredMember1);
	}

	@Test
	@DisplayName("Customer 의 PK를 Seller 의 PK 검색 메서드로 조회할 수 없다")
	void findBySellerException() {
		Member member1 = new Member();
		member1.setUserId("noogler0258");
		member1.setPostalCode("129-01");
		member1.setValid(true);
		member1.setPassword("test1");
		member1.setAddress("서울특별시 광진구 자양대로");
		member1.setPhone("010-4832-2000");
		member1.setUsername("김윤식");
		Member registeredMember1 = memberService.register(member1, MemberType.ROLE_CUSTOMER);
		Long memberPk = registeredMember1.getMemberPk();

		Assertions.assertThrows(RuntimeException.class, () -> memberService.findSellerById(memberPk));
	}

	@Test
	@DisplayName("UserType 에 따른 전체 사용자를 조회할 수 있다.")
	void findByUserType() {
		Member member1 = new Member();
		member1.setUserId("noogler0258");
		member1.setPostalCode("129-01");
		member1.setValid(true);
		member1.setPassword("test1");
		member1.setAddress("서울특별시 광진구 자양대로");
		member1.setPhone("010-4832-2000");
		member1.setUsername("김윤식");
		Member registeredMember1 = memberService.register(member1, MemberType.ROLE_CUSTOMER);

		Member member2 = new Member();
		member2.setUserId("noogler02");
		member2.setPostalCode("129-03");
		member2.setValid(true);
		member2.setPassword("test2");
		member2.setAddress("경기도 남양주시 경춘로");
		member2.setPhone("010-4822-2020");
		member2.setUsername("홍길동");
		memberService.register(member2, MemberType.ROLE_SELLER);

		Member member3 = new Member();
		member3.setUserId("noogler");
		member3.setPostalCode("129-03");
		member3.setValid(true);
		member3.setPassword("test2");
		member3.setAddress("경기도 남양주시 경춘로");
		member3.setPhone("010-4822-2020");
		member3.setUsername("홍길동");
		memberService.register(member3, MemberType.ROLE_SELLER);

		List<Member> customers = memberService.findByUserType(MemberType.ROLE_CUSTOMER);
		List<Member> sellers = memberService.findByUserType(MemberType.ROLE_SELLER);

		org.assertj.core.api.Assertions.assertThat(customers).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(sellers).hasSize(2);
	}
}
