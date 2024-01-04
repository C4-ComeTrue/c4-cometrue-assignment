package org.c4marathon.assignment.domain.consumer.controller;

import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.service.ConsumerService;
import org.c4marathon.assignment.global.auth.ConsumerThreadLocal;
import org.c4marathon.assignment.global.response.ResponseDto;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/consumers/orders")
public class ConsumerController {

	private final ConsumerService consumerService;

	@PostMapping
	public ResponseDto<Void> purchaseProduct(@RequestBody @Valid PurchaseProductRequest request) {
		consumerService.purchaseProduct(request, ConsumerThreadLocal.get());
		return ResponseDto.message("success purchase product");
	}

	@DeleteMapping("/{order_id}")
	public ResponseDto<Void> refundOrder(@PathVariable("order_id") Long orderId) {
		consumerService.refundOrder(orderId, ConsumerThreadLocal.get());
		return ResponseDto.message("success refund product");
	}

	@PatchMapping("/{order_id}")
	public ResponseDto<Void> confirmOrder(@PathVariable("order_id") Long orderId) {
		consumerService.confirmOrder(orderId, ConsumerThreadLocal.get());
		return ResponseDto.message("success confirm order");
	}
}
