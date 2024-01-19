package org.c4marathon.assignment.service;

import java.util.List;

import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.Sales;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.SalesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalesService {

	private final SalesRepository salesRepository;

	@Transactional(readOnly = true)
	public List<Sales> getSalesBySeller(Member seller) {
		if (seller.getMemberType() != MemberType.ROLE_SELLER) {
			throw ErrorCd.NO_PERMISSION.serviceException("판매자만 접근할 수 있는 기능입니다");
		}

		return salesRepository.findAllBySeller(seller);
	}

	@Transactional
	// Sales에는 기록이 추가만 될뿐, 삭제는 되지 않음(결제 환불한다고 결제 기록이 날아가지 않는 원리)
	public void addSales(OrderItem item, Member from, Member to, int value, ChargeType chargeType) {
		Sales sales = new Sales();
		sales.setCustomer(from);
		sales.setSeller(to);
		sales.setOrderItem(item);
		sales.setValue(value);
		sales.setChargeType(chargeType);
		salesRepository.save(sales);
	}

}
