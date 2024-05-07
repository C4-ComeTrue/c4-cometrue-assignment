package org.c4marathon.assignment.domain.seller.service;

import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.repository.ProductRepository;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.dto.request.PutProductRequest;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SellerService {

	private final ProductRepository productRepository;
	private final ProductReadService productReadService;

	/**
	 * 상품 업로드
	 * 같은 판매자가 같은 상품의 이름을 올리면 예외처리
	 */
	@Transactional
	public void putProduct(PutProductRequest request, Seller seller) {
		if (Boolean.TRUE.equals(productReadService.existsByNameAndSeller(request.name(), seller))) {
			throw ErrorCode.ALREADY_PRODUCT_NAME_EXISTS
				.baseException("seller id: %d, product name: %s", seller.getId(), request.name());
		}
		saveProduct(request, seller);
	}

	/**
	 * Product 저장
	 */
	private void saveProduct(PutProductRequest request, Seller seller) {
		productRepository.save(Product.builder()
			.name(request.name())
			.amount(request.amount())
			.stock(request.stock())
			.description(request.description())
			.seller(seller)
			.build());
	}
}
