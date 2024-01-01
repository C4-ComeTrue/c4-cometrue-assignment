package org.c4marathon.assignment.domain.consumer.controller;

import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.service.ConsumerService;
import org.c4marathon.assignment.global.auth.ConsumerThreadLocal;
import org.c4marathon.assignment.global.response.ResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/consumers")
public class ConsumerController {

	private final ConsumerService consumerService;

	@PostMapping("/purchase")
	public ResponseDto<Void> purchaseProduct(@RequestBody @Valid PurchaseProductRequest request) {
		consumerService.purchaseProduct(request, ConsumerThreadLocal.get());
		return ResponseDto.message("success purchase product");
	}
}
