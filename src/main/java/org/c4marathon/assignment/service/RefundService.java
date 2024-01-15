package org.c4marathon.assignment.service;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Refund;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RefundService {

	private final MemberService memberService;

	private final RefundRepository refundRepository;

	public Refund save(Refund refund){
		if (refund.getSeller().getMemberType() != MemberType.ROLE_SELLER){
			throw ErrorCd.INTERNAL_SERVER_ERROR.serviceException("반품 요청 대상 판매자가 유효하지 않습니다.");
		}

		if (refund.getCustomer().getMemberType() != MemberType.ROLE_CUSTOMER) {
			throw ErrorCd.INTERNAL_SERVER_ERROR.serviceException("반품을 요청한 사용자 정보가 유효하지 않습니다.");
		}

		return refundRepository.save(refund);
	}

	public Refund findById(Long refundId){
		Optional<Refund> optionalRefund = refundRepository.findById(refundId);

		if (optionalRefund.isEmpty()){
			throw ErrorCd.NO_SUCH_ITEM.serviceException("반품 건을 조회할 수 없습니다");
		}

		return optionalRefund.get();
	}

	public List<Refund> findBySeller(Member seller){
		List<Refund> refundBySeller = refundRepository.findRefundBySeller(seller);

		if (refundBySeller.isEmpty()){
			throw ErrorCd.NO_SUCH_ITEM.serviceException("판매자에게서 조회되는 반품건이 없습니다.");
		}
		return refundBySeller;
	}
}
