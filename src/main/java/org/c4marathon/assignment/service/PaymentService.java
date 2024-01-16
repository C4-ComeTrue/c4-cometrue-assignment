package org.c4marathon.assignment.service;

import java.util.List;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.Payment;
import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.MemberRepository;
import org.c4marathon.assignment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final MemberService memberService;

	@Transactional
	// 소비자(Member)의 계좌에 value 금액을 충전한다.
	public Payment charge(int value, Member member) {

		if (value <= 0){
			throw ErrorCd.INVALID_ARGUMENT.serviceException("충전 금액을 확인하세요",
				"잘못된 충전 금액 (입력값) : ", value);
		}

		Payment payment = new Payment(
			member,
			ChargeType.CHARGE,
			value
		);

		paymentRepository.save(payment);
		return payment;
	}

	@Transactional
	// 소비자(Member)의 계좌에 value 금액을 소진한다.
	public Payment discharge(int value, Member member){

		if (value <= 0) {
			throw ErrorCd.INTERNAL_SERVER_ERROR.serviceException("결제 시도 금액 이상", value);
		}

		if (getBalance(member.getMemberPk()) < value) {
			throw ErrorCd.INVALID_ARGUMENT.serviceException("결제 이상", "한도 초과");
		}

		Payment payment = new Payment(
			member,
			ChargeType.DISCHARGE,
			value
		);

		return paymentRepository.save(payment);
	}

	@Transactional
	// 판매자(Seller)의 계좌에 value 금액을 충전한다.
	public void sellerCharge(int value, Member member) {
		if ( member.getMemberType() != MemberType.ROLE_SELLER ) {
			throw ErrorCd.NO_PERMISSION.serviceException("판매자만 수익을 올릴 수 있습니다.");
		}

		Payment payment = new Payment(
			member,
			ChargeType.COMMISSION,
			value
		);

		paymentRepository.save(payment);
	}

	@Transactional
	// 판매자(Seller)의 계좌에 value 금액을 소진한다.
	// 요구 조건 상, 구매 확인 이전까지만 환불이 가능함.
	// 따라서 구매 확정 전에는 seller 잔고는 변화할 일이 없음.
	// 그러나, 통상적인 경우 판매 확인 이후에도 반품이 가능하기 때문에 이 메서드를 유지함.
	public Payment sellerDischarge(int value, Member member) {
		if ( member.getMemberType() != MemberType.ROLE_SELLER ) {
			throw ErrorCd.NO_PERMISSION.serviceException("판매자만 반품 지출을 생성할 수 있습니다.");
		}
		Payment payment = new Payment(
			member,
			ChargeType.REFUND,
			value
		);

		return paymentRepository.save(payment);

	}

	// 특정 사용자의 잔고 금액을 조회한다.
	private int getBalance(Long memberId) {
		Member member = memberService.findCustomerId(memberId);
		return paymentRepository.currentBalance(member);
	}

}
