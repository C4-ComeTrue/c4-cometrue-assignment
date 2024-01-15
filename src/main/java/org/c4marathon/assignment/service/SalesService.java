package org.c4marathon.assignment.service;

import java.util.List;

import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.Sales;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.MemberRepository;
import org.c4marathon.assignment.repository.SalesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SalesService {

	private final OrderService orderService;
	private final PaymentService paymentService;

	private final SalesRepository salesRepository;
	private final MemberRepository memberRepository;

	public List<Sales> getSalesBySeller(Member seller){
		if(seller.getMemberType() != MemberType.ROLE_SELLER) {
			throw ErrorCd.NO_PERMISSION.serviceException("판매자만 접근할 수 있는 기능입니다");
		}

		return salesRepository.findAllBySeller(seller);
	}

	// 제품 구입이 이뤄지는 경우 제품 가액만큼 회사 계좌로 입금됨.
	protected void addSales(OrderItem item, Member from, Member to, int value, ChargeType chargeType) {
		Sales sales = new Sales();
		sales.setCustomer(from);
		sales.setSeller(to);
		sales.setSeller(item.getItem().getSeller());
		sales.setOrderItem(item);
		sales.setValue(value);
		sales.setChargeType(chargeType);
		salesRepository.save(sales);
	}

	// 제품 구매가 확인되는 경우 5%를 제하고 판매자에게 지급됨.
	protected void commissionProcedure(Long orderId, Long orderItemId, Member customer, Member seller) {
		Order order = orderService.findById(orderId);
		Member admin = memberRepository.findFinancialAdmin();

		// 주문에 포함된 상품의 승인인지 검증.
		OrderItem orderItem = orderItemValidation(order, orderItemId);

		// 1. SalesRepository에서 전체 금액을 차감함.
		addSales(orderItem, customer, admin, orderItem.getTotalPrice(), ChargeType.DISCHARGE);

		// 2. SalesRepository에 수수료 금액을 추가함.
		addSales(orderItem, customer, admin, orderItem.getTotalPrice() - commissionCalculation(orderItem), ChargeType.COMMISSION);

		// 3. Seller의 계좌로 물품가액이 입금됨.
		paymentService.sellerCharge(commissionCalculation(orderItem), orderItem.getItem().getSeller());

		// 4. Seller에 물품가액이 지급된것이 기록됨.
		addSales(orderItem, admin, seller, commissionCalculation(orderItem), ChargeType.CHARGE);

		// 5. 회사 계좌로부터 Seller에게 지급할 금액이 빠져나간것이 기록됨.
		addSales(orderItem, admin, seller, commissionCalculation(orderItem), ChargeType.DISCHARGE);
	}

	private int commissionCalculation(OrderItem orderItem){
		int totalPrice = orderItem.getTotalPrice();
		return (int) (totalPrice - (totalPrice * 0.05));
	}

	private OrderItem orderItemValidation(Order order, Long orderItemId){
		List<OrderItem> orderItems = order.getOrderItems();

		for (OrderItem orderItem : orderItems) {
			if (orderItemId.equals(orderItem.getOrderItemPk())) {
				return orderItem;
			}
		}

		throw ErrorCd.INVALID_ARGUMENT.serviceException("주문에 포함된 상품 승인이 아님", "부적절한 접근");

	}

}
