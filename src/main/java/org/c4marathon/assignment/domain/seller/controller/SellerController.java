package org.c4marathon.assignment.domain.seller.controller;

import org.c4marathon.assignment.domain.seller.dto.request.PutProductRequest;
import org.c4marathon.assignment.domain.seller.service.SellerService;
import org.c4marathon.assignment.global.auth.SellerThreadLocal;
import org.c4marathon.assignment.global.response.ResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {

	private final SellerService sellerService;

	@PostMapping("/products")
	public ResponseDto<Void> putProduct(PutProductRequest request) {
		sellerService.putProduct(request, SellerThreadLocal.get());
		return ResponseDto.message("success put product");
	}
}
