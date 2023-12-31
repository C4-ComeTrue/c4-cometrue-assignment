package org.c4marathon.assignment.domain.seller.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.c4marathon.assignment.global.error.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SellerService {

	private final SellerRepository sellerRepository;

	public void signup(SignUpRequest request) {
		if (sellerRepository.existsByEmail(request.getEmail())) {
			throw new BaseException(ALREADY_CONSUMER_EXISTS);
		}

		saveSeller(request);
	}

	@Transactional
	public void saveSeller(SignUpRequest request) {
		sellerRepository.save(Seller.builder()
			.email(request.getEmail())
			.build());
	}
}
