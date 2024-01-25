package org.c4marathon.assignment.bankaccount.config;

import org.c4marathon.assignment.bankaccount.product.ProductManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductConfiguration {

	@Bean
	public ProductManager productManager() {
		ProductManager manager = new ProductManager();
		manager.init();
		return manager;
	}
}
