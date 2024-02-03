package org.c4marathon.assignment.bankaccount.product;

import java.util.HashMap;
import java.util.Map;

public class ProductManager {
	private static final String[] productNames = {"free", "regular"};
	private static final int[] productRates = {500, 300};

	private Map<String, Integer> productInfo = new HashMap<>();

	public Map<String, Integer> getProductInfo() {
		return productInfo;
	}

	public Integer getRate(String productName) {
		return productInfo.get(productName);
	}

	public void init() {
		int length = productNames.length;
		for (int i = 0; i < length; i++) {
			productInfo.put(productNames[i], productRates[i]);
		}
	}
}
