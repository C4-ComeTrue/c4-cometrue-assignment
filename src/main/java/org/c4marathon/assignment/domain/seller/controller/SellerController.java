package org.c4marathon.assignment.domain.seller.controller;

import org.c4marathon.assignment.domain.seller.dto.request.PutProductRequest;
import org.c4marathon.assignment.domain.seller.service.SellerService;
import org.c4marathon.assignment.global.auth.SellerThreadLocal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {

	private final SellerService sellerService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/products")
	public void putProduct(@RequestBody @Valid PutProductRequest request) {
		sellerService.putProduct(request, SellerThreadLocal.get());
	}
}
