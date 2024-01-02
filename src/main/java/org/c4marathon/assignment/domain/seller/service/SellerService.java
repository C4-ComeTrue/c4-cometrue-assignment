package org.c4marathon.assignment.domain.seller.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.repository.ProductRepository;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.dto.request.PutProductRequest;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.c4marathon.assignment.global.error.BaseException;
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
		if (sellerRepository.existsByEmail(request.getEmail())) {
			throw new BaseException(ALREADY_SELLER_EXISTS);
		}

		saveSeller(request);
	}

	private void saveSeller(SignUpRequest request) {
		sellerRepository.save(Seller.builder()
			.email(request.getEmail())
			.build());
	}

	public void putProduct(PutProductRequest request, Seller seller) {
		if (productReadService.existsByNameAndSeller(request.getName(), seller)) {
			throw new BaseException(ALREADY_PRODUCT_NAME_EXISTS);
		}
		saveProduct(request, seller);
	}

	private void saveProduct(PutProductRequest request, Seller seller) {
		productRepository.save(Product.builder()
			.name(request.getName())
			.amount(request.getAmount())
			.stock(request.getStock())
			.description(request.getDescription())
			.seller(seller)
			.build());
	}
}
