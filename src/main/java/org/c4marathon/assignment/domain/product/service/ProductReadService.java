package org.c4marathon.assignment.domain.product.service;

import org.c4marathon.assignment.domain.product.repository.ProductRepository;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReadService {

	private final ProductRepository productRepository;

	public Boolean existsByNameAndSeller(String name, Seller seller) {
		return productRepository.existsByNameAndSeller(name, seller);
	}
}
