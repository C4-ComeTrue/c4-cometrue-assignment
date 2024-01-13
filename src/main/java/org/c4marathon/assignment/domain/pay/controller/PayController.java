package org.c4marathon.assignment.domain.pay.controller;

import org.c4marathon.assignment.domain.pay.dto.request.ChargePayRequest;
import org.c4marathon.assignment.domain.pay.service.PayService;
import org.c4marathon.assignment.global.auth.ConsumerThreadLocal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/consumers/pay")
public class PayController {

	private final PayService payService;

	@PostMapping
	public void chargePay(@RequestBody @Valid ChargePayRequest request) {
		payService.chargePay(request, ConsumerThreadLocal.get());
	}
}
