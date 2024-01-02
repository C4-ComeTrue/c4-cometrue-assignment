package org.c4marathon.assignment.domain.deliverycompany.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.c4marathon.assignment.global.error.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DeliveryCompanyReadService {

	private final DeliveryCompanyRepository deliveryCompanyRepository;

	public Boolean existsByEmail(String email) {
		return deliveryCompanyRepository.existsByEmail(email);
	}

	public DeliveryCompany findMinimumCountOfDelivery() {
		return deliveryCompanyRepository.findMinimumCountOfDelivery()
			.orElseThrow(() -> new BaseException(DELIVERY_COMPANY_NOT_FOUND));
	}
}
