package org.c4marathon.assignment.domain.seller.service;

import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerReadService {

	private final SellerRepository sellerRepository;

	public boolean existsByEmail(String email) {
		return sellerRepository.existsByEmail(email);
	}
}
