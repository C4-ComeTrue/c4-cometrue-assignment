package org.c4marathon.assignment.domain.product.service;

import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.repository.ProductRepository;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReadService {

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public Boolean existsByNameAndSeller(String name, Seller seller) {
		return productRepository.existsByNameAndSeller(name, seller);
	}

	@Transactional(readOnly = true)
	public Product findById(Long id) {
		return productRepository.findByIdJoinFetch(id)
			.orElseThrow(() -> ErrorCode.PRODUCT_NOT_FOUND.baseException("id: %d", id));
	}
}
