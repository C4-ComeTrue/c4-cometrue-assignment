package org.c4marathon.assignment.domain.product.service;

import java.util.List;

import org.c4marathon.assignment.domain.product.dto.request.ProductSearchRequest;
import org.c4marathon.assignment.domain.product.dto.response.ProductSearchResponse;
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

	/**
	 * Seller와 product.name으로 Product 존재 여부 확인
	 */
	@Transactional(readOnly = true)
	public Boolean existsByNameAndSeller(String name, Seller seller) {
		return productRepository.existsByNameAndSeller(name, seller);
	}

	/**
	 * Seller와 조인한 Product를 id로 조회
	 */
	@Transactional(readOnly = true)
	public Product findById(Long id) {
		return productRepository.findByIdJoinFetch(id)
			.orElseThrow(() -> ErrorCode.PRODUCT_NOT_FOUND.baseException("id: %d", id));
	}

	@Transactional(readOnly = true)
	public Long findReviewCount(Long productId) {
		return productRepository.findReviewCount(productId);
	}

	/**
	 * sortType에 해당하는 조건으로 pagination
	 * Newest: 최신순, created_at desc, product_id asc
	 * PriceAsc: 가격 낮은 순, amount asc, product_id asc
	 * PriceDesc: 가격 높은 순, amount desc, product_id asc
	 * Popularity: 인기 순(주문 많은 순), order_count desc, product_id asc
	 * TopRated: 평점 높은 순(review score), avg_score desc, product_id asc
	 */
	@Transactional(readOnly = true)
	public ProductSearchResponse searchProduct(ProductSearchRequest request) {
		String keyword = toQueryKeyword(request.keyword());
		Long productId = request.productId();
		int pageSize = request.pageSize();

		List<Product> products = switch (request.sortType()) {
			case NEWEST -> productRepository.findByNewest(keyword, request.createdAt(), productId, pageSize);
			case PRICE_ASC -> productRepository.findByPriceAsc(keyword, request.amount(), productId, pageSize);
			case PRICE_DESC -> productRepository.findByPriceDesc(keyword, request.amount(), productId, pageSize);
			case POPULARITY -> productRepository.findByPopularity(keyword, request.orderCount(), productId, pageSize);
			case TOP_RATED -> productRepository.findByTopRated(keyword, request.score(), productId, pageSize);
		};
		return ProductSearchResponse.of(products);
	}

	private String toQueryKeyword(String keyword) {
		return "%" + keyword + "%";
	}
}
