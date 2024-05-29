package org.c4marathon.assignment.domain.product.controller;

import org.c4marathon.assignment.domain.product.dto.request.ProductSearchRequest;
import org.c4marathon.assignment.domain.product.dto.response.ProductSearchResponse;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

	private final ProductReadService productReadService;

	@GetMapping
	public ProductSearchResponse searchProduct(
		@Valid @ModelAttribute ProductSearchRequest request
	) {
		return productReadService.searchProduct(request);
	}
}
