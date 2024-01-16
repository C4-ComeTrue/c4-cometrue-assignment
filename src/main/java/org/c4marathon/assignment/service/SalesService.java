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
@RequiredArgsConstructor
public class SalesService {

	private final SalesRepository salesRepository;

	@Transactional(readOnly = true)
	public List<Sales> getSalesBySeller(Member seller){
		if(seller.getMemberType() != MemberType.ROLE_SELLER) {
			throw ErrorCd.NO_PERMISSION.serviceException("판매자만 접근할 수 있는 기능입니다");
		}

		return salesRepository.findAllBySeller(seller);
	}

	@Transactional
	// 제품 구입이 이뤄지는 경우 제품 가액만큼 회사 계좌로 입금됨.
	public void addSales(OrderItem item, Member from, Member to, int value, ChargeType chargeType) {
		Sales sales = new Sales();
		sales.setCustomer(from);
		sales.setSeller(to);
		sales.setSeller(item.getItem().getSeller());
		sales.setOrderItem(item);
		sales.setValue(value);
		sales.setChargeType(chargeType);
		salesRepository.save(sales);
	}

}
