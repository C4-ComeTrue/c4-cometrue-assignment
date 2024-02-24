package org.c4marathon.assignment.bankaccount.product;

import java.util.List;

import org.c4marathon.assignment.bankaccount.dto.response.SavingProductResponseDto;
import org.c4marathon.assignment.bankaccount.entity.SavingProduct;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.repository.SavingProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductManager {

	private final SavingProductRepository savingProductRepository;

	@Cacheable(value = "savingProduct")
	public List<SavingProductResponseDto> getProductInfo() {
		List<SavingProduct> productList = savingProductRepository.findAll();
		return productList.stream()
			.map(SavingProductResponseDto::new)
			.toList();
	}

	@Cacheable(value = "savingProduct", key = "#productName")
	public int getRate(String productName) {
		return savingProductRepository.findRateByProductName(productName)
			.orElseThrow(() -> AccountErrorCode.PRODUCT_NOT_FOUND.accountException(
				"존재하지 않는 적금 상품 이름, productName = " + productName));
	}
}
