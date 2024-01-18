package org.c4marathon.assignment.service;

import java.util.List;

import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.Payment;
import org.c4marathon.assignment.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonetaryTransactionService {

	private final SalesService salesService;
	private final PaymentService paymentService;

	private final MemberRepository memberRepository;


	// 제품 구매가 확인되는 경우 5%를 제하고 판매자에게 지급됨.
	public void commissionProcedure(OrderItem orderItem, Member customer, Member seller) {
		Member admin = memberRepository.findFinancialAdmin();

		// 1. SalesRepository에서 전체 금액을 차감함.
		salesService.addSales(orderItem, customer, admin, orderItem.generateTotalPrice(), ChargeType.DISCHARGE);

		// 2. SalesRepository에 수수료 금액을 추가함.
		salesService.addSales(orderItem, customer, admin,
			orderItem.generateTotalPrice() - commissionCalculation(orderItem), ChargeType.COMMISSION);

		// 3. Seller의 계좌로 물품가액이 입금됨.
		paymentService.sellerCharge(commissionCalculation(orderItem), orderItem.getItem().getSeller());

		// 4. Seller에 물품가액이 지급된것이 기록됨.
		salesService.addSales(orderItem, admin, seller, commissionCalculation(orderItem), ChargeType.CHARGE);

		// 5. 회사 계좌로부터 Seller에게 지급할 금액이 빠져나간것이 기록됨.
		salesService.addSales(orderItem, admin, seller, commissionCalculation(orderItem), ChargeType.DISCHARGE);
	}

	private int commissionCalculation(OrderItem orderItem) {
		int totalPrice = orderItem.generateTotalPrice();
		return (int) (totalPrice - (totalPrice * 0.05));
	}

	// 제품 판매시 금액 이동
	public Payment transactionsForSelling(List<OrderItem> orderItems, Member customer) {
		int itemsTotalPrice = 0;

		// FinancialAdmin은 단 하나만 존재하며 모든 구매 반품 흐름은 이 admin을 통해 확인함.
		Member admin = memberRepository.findFinancialAdmin();

		for (OrderItem orderItem : orderItems) {
			itemsTotalPrice += orderItem.generateTotalPrice();
			// 기업 잔액 변동 적용 (매출) - 제품별로 구매를 각각 기록함.
			salesService.addSales(orderItem, customer, admin, orderItem.generateTotalPrice(), ChargeType.CHARGE);
		}

		// 고객의 잔고에서 구매합계 금액을 출금처리함.
		return paymentService.discharge(itemsTotalPrice, customer);
	}

	// 제품 반품시 금액 이동, 이전 메서드와 차감 -> 충전을 제외하고 로직 동일.
	public Payment transactionsForRefunding(List<OrderItem> orderItems, Member customer) {
		int itemsTotalPrice = 0;
		Member admin = memberRepository.findFinancialAdmin();

		for (OrderItem orderItem : orderItems) {
			itemsTotalPrice += orderItem.generateTotalPrice();
			// 기업 잔액 변동 적용 (매출)
			salesService.addSales(orderItem, admin, customer, orderItem.generateTotalPrice(), ChargeType.REFUND);
		}

		return paymentService.charge(itemsTotalPrice, customer);
	}

}
