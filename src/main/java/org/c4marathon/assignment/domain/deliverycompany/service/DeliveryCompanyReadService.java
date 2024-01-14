package org.c4marathon.assignment.domain.deliverycompany.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DeliveryCompanyReadService {

	private final DeliveryCompanyRepository deliveryCompanyRepository;

	/**
	 * email로 DeliveryCompany 존재 여부 확인
	 */
	@Transactional(readOnly = true)
	public Boolean existsByEmail(String email) {
		return deliveryCompanyRepository.existsByEmail(email);
	}

	/**
	 * @return: 가장 적은 배송을 담당하고있는 DeliveryCompany
	 */
	@Transactional(readOnly = true)
	public DeliveryCompany findMinimumCountOfDelivery() {
		return deliveryCompanyRepository.findMinimumCountOfDelivery()
			.orElseThrow(DELIVERY_COMPANY_NOT_FOUND::baseException);
	}
}
