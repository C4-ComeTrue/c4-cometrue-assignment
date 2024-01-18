package org.c4marathon.assignment.service;

import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Payment;
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
class PaymentServiceTest {

	private static Long sellerId;
	private static Long customerId;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private MemberService memberService;

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
	}

	@Test
	@DisplayName("고객 잔고에 10000원을 충전합니다.")
	void chargingCustomer() {
		Member customer = memberService.findCustomerById(customerId);
		Payment charged = paymentService.charge(10000, customer);

		// 1. Payment Table 입력 검증.
		Assertions.assertEquals(charged.getValue(), 10000);
		Assertions.assertEquals(charged.getValueType(), ChargeType.CHARGE);
		Assertions.assertEquals(charged.getMember(),customer);

		// 2. 잔고 금액 정상 여부 검증
		Assertions.assertEquals(charged.getValue(), paymentService.getBalance(customerId));
	}

	@Test
	@DisplayName("판매자의 잔고에 10000원을 충전합니다.")
	void chargingSeller() {
		Member Seller = memberService.findSellerById(sellerId);
		Payment discharged = paymentService.charge(10000, Seller);

		// 1. Payment Table 잔고 충전 정보 입력값 검증.
		Assertions.assertEquals(discharged.getValue(),10000);
		Assertions.assertEquals(discharged.getValueType(),ChargeType.CHARGE);
		Assertions.assertEquals(discharged.getMember(),Seller);

		// 2. 잔고 금액 정상 여부 검증
		Assertions.assertEquals(discharged.getValue(), paymentService.getBalance(sellerId));
	}

	@Test
	@DisplayName("고객 잔고에 10000원을 충전합니다.")
	void chargingCustomerValueException() {
		Member customer = memberService.findCustomerById(customerId);

		RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class,
			() -> paymentService.charge(-10000, customer));

		Assertions.assertEquals(runtimeException.getMessage(),
			"INVALID_ARGUMENT : Invalid Argument - 충전 금액을 확인하세요");
	}

	@Test
	@DisplayName("판매자의 잔고에 10000원을 충전합니다.")
	void chargingSellerValueException() {
		Member seller = memberService.findSellerById(sellerId);
		RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class,
			() -> paymentService.charge(-1*10000, seller));

		Assertions.assertEquals(runtimeException.getMessage(),
			"INVALID_ARGUMENT : Invalid Argument - 충전 금액을 확인하세요");
	}

	@Test
	@DisplayName("고객 잔고에 10000원을 소진합니다.")
	void disChargingCustomer() {
		Member customer = memberService.findCustomerById(customerId);
		Payment charged = paymentService.charge(20000, customer);
		Payment discharged = paymentService.discharge(10000, customer);

		// 1. Payment Table 잔고 소진 정보 입력값 검증.
		Assertions.assertEquals(discharged.getValue(),10000);
		Assertions.assertEquals(discharged.getValueType(),ChargeType.DISCHARGE);
		Assertions.assertEquals(discharged.getMember(),customer);

		// 2. 잔고 금액 정상 여부 검증
		int paymentResultValue = charged.getValue() - discharged.getValue();
		int assertionTargetValue = paymentService.getBalance(customerId);

		Assertions.assertEquals(assertionTargetValue, paymentResultValue);
	}

	@Test
	@DisplayName("판매자 잔고에 10000원을 소진합니다.")
	void disChargingSeller() {
		Member seller = memberService.findSellerById(sellerId);
		Payment charged = paymentService.charge(20000, seller);
		Payment discharged = paymentService.discharge(10000, seller);

		// 1. Payment Table 잔고 소진 정보 입력값 검증.
		Assertions.assertEquals(discharged.getValue(),10000);
		Assertions.assertEquals(discharged.getValueType(),ChargeType.DISCHARGE);
		Assertions.assertEquals(discharged.getMember(),seller);

		// 2. 잔고 금액 정상 여부 검증
		int paymentResultValue = charged.getValue() - discharged.getValue();
		int assertionTargetValue = paymentService.getBalance(sellerId);

		Assertions.assertEquals(assertionTargetValue,
			paymentResultValue);
	}

	@Test
	@DisplayName("고객 잔고가 부족한 경우 결제가 이뤄지지 않습니다.")
	void disChargingCustomerException() {
		Member customer = memberService.findCustomerById(customerId);

		// 잔고가 부족한 상태에서 결제 시도시 Exception 발생
		RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class,
			() -> paymentService.discharge(10000, customer));
		Assertions.assertEquals(runtimeException.getMessage(),
			"INVALID_ARGUMENT : Invalid Argument - 결제 이상");
	}

	@Test
	@DisplayName("판매자의 잔고가 부족한 경우 결제가 이뤄지지 않습니다")
	void disChargingSellerException() {
		Member seller = memberService.findSellerById(sellerId);

		// 잔고가 부족한 상태에서 결제 시도시 Exception 발생
		RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class,
			() -> paymentService.discharge(10000, seller));
		Assertions.assertEquals(runtimeException.getMessage(),
			"INVALID_ARGUMENT : Invalid Argument - 결제 이상");
	}

	@Test
	@DisplayName("COMMISSION - CHARGE는 입금으로,"
		+ " REFUND - DISCHARGE는 출금으로 취급하여 잔고를 조회합니다.")
	void balance() {
		Member seller = memberService.findSellerById(sellerId);
		Payment chargeSeller = paymentService.charge(20000, seller);
		Payment dischargeSeller = paymentService.discharge(10000, seller);

		int expectedResultSeller = chargeSeller.getValue() - dischargeSeller.getValue();
		int targetResultSeller = paymentService.getBalance(sellerId);

		Assertions.assertEquals(expectedResultSeller,targetResultSeller);

		Member customer = memberService.findCustomerById(customerId);
		Payment chargeCustomer = paymentService.charge(20000, customer);
		Payment dischargeCustomer = paymentService.discharge(10000, customer);

		int expectedResultCustomer = chargeCustomer.getValue() - dischargeCustomer.getValue();
		int targetResultCustomer = paymentService.getBalance(customerId);

		Assertions.assertEquals(expectedResultCustomer,targetResultCustomer);
	}

	@Test
	@DisplayName("판매자의 계좌에 수익을 올립니다. (단순 입금과는 상이함)")
	void sellerCharge(){
		Member seller = memberService.findSellerById(sellerId);
		Payment chargeSeller = paymentService.sellerCharge(20000, seller);
		Payment dischargeSeller = paymentService.discharge(10000, seller);

		int expectedResultSeller = chargeSeller.getValue() - dischargeSeller.getValue();
		int targetResultSeller = paymentService.getBalance(sellerId);

		Assertions.assertEquals(expectedResultSeller,targetResultSeller);
	}

	@Test
	@DisplayName("소비자는 판매자 수익 추가 메서드를 사용할 수 없습니다.")
	void sellerChargeException(){
		Member customer = memberService.findCustomerById(customerId);

		RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class,
			() -> paymentService.sellerCharge(20000, customer));
		Assertions.assertEquals(runtimeException.getMessage(),
			"NO_PERMISSION : No Permission - 판매자만 수익을 올릴 수 있습니다.");
	}


}
