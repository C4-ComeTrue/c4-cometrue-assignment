package org.c4marathon.assignment.service;

import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Payment;
import org.c4marathon.assignment.exception.ErrorCd;
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

		if (value <= 0) {
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
	public Payment discharge(int value, Member member) {

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
	public Payment sellerCharge(int value, Member member) {
		if ( member.getMemberType() != MemberType.ROLE_SELLER ) {
			throw ErrorCd.NO_PERMISSION.serviceException("판매자만 수익을 올릴 수 있습니다.");
		}

		Payment payment = new Payment(
			member,
			ChargeType.COMMISSION,
			value
		);

		return paymentRepository.save(payment);
	}

	// 특정 사용자의 잔고 금액을 조회한다.
	public int getBalance(Long memberId) {
		Member member = memberService.findById(memberId);
		int totalCharged = paymentRepository.totalCharged(member);
		int totalDischarged = (-1) * paymentRepository.totalDischarged(member);
		return totalCharged + totalDischarged;
	}

}
