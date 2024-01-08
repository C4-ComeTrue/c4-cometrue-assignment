package org.c4marathon.assignment.domain.seller.service;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.repository.ProductRepository;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.dto.request.PutProductRequest;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class SellerService {

	private final SellerRepository sellerRepository;
	private final ProductRepository productRepository;
	private final ProductReadService productReadService;

	public void signup(SignUpRequest request) {
		if (sellerRepository.existsByEmail(request.email())) {
			throw ErrorCode.ALREADY_SELLER_EXISTS.baseException("email: %s", request.email());
		}

		saveSeller(request);
	}

	private void saveSeller(SignUpRequest request) {
		sellerRepository.save(Seller.builder()
			.email(request.email())
			.build());
	}

	public void putProduct(PutProductRequest request, Seller seller) {
		if (productReadService.existsByNameAndSeller(request.name(), seller)) {
			throw ErrorCode.ALREADY_PRODUCT_NAME_EXISTS
				.baseException("seller id: %d, product name: %s", seller.getId(), request.name());
		}
		saveProduct(request, seller);
	}

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
