package org.c4marathon.assignment.bankaccount.dto.response;

import org.c4marathon.assignment.bankaccount.entity.SavingProduct;

public record SavingProductResponseDto(
	long productPk,
	String productName,
	int productRate
) {
	public SavingProductResponseDto(SavingProduct savingProduct) {
		this(savingProduct.getProductPk(), savingProduct.getProductName(), savingProduct.getProductRate());
	}
}
