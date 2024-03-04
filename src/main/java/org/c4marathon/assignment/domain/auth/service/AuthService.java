package org.c4marathon.assignment.domain.auth.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.consumer.service.ConsumerReadService;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.c4marathon.assignment.domain.seller.service.SellerReadService;
import org.c4marathon.assignment.global.constant.MemberType;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final ConsumerReadService consumerReadService;
	private final ConsumerRepository consumerRepository;
	private final SellerReadService sellerReadService;
	private final SellerRepository sellerRepository;
	private final DeliveryCompanyReadService deliveryCompanyReadService;
	private final DeliveryCompanyRepository deliveryCompanyRepository;

	/**
	 * 회원 가입
	 * @param request address와 email
	 */
	public void signup(SignUpRequest request, MemberType memberType) {
		switch (memberType) {
			case CONSUMER -> consumerSignup(request);
			case SELLER -> sellerSignup(request);
			case DELIVERY_COMPANY -> deliveryCompanySignup(request);
		}
	}

	private void consumerSignup(SignUpRequest request) {
		if (request.address() == null) {
			throw CONSUMER_NEED_ADDRESS.baseException();
		}
		if (Boolean.TRUE.equals(consumerReadService.existsByEmail(request.email()))) {
			throw ALREADY_CONSUMER_EXISTS.baseException("email: %s", request.email());
		}
		saveConsumer(request);
	}

	private void sellerSignup(SignUpRequest request) {
		if (Boolean.TRUE.equals(sellerReadService.existsByEmail(request.email()))) {
			throw ErrorCode.ALREADY_SELLER_EXISTS.baseException("email: %s", request.email());
		}
		saveSeller(request);
	}

	private void deliveryCompanySignup(SignUpRequest request) {
		if (Boolean.TRUE.equals(deliveryCompanyReadService.existsByEmail(request.email()))) {
			throw ALREADY_DELIVERY_COMPANY_EXISTS.baseException("email: %s", request.email());
		}
		saveDeliveryCompany(request);
	}

	private void saveConsumer(SignUpRequest request) {
		consumerRepository.save(new Consumer(request.email(), request.address()));
	}

	private void saveSeller(SignUpRequest request) {
		sellerRepository.save(new Seller(request.email()));
	}

	private void saveDeliveryCompany(SignUpRequest request) {
		deliveryCompanyRepository.save(new DeliveryCompany(request.email()));
	}
}
