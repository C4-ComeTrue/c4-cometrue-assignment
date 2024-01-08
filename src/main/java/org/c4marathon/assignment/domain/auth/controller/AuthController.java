package org.c4marathon.assignment.domain.auth.controller;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.consumer.service.ConsumerService;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyService;
import org.c4marathon.assignment.domain.seller.service.SellerService;
import org.c4marathon.assignment.global.constant.MemberType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final ConsumerService consumerService;
	private final SellerService sellerService;
	private final DeliveryCompanyService deliveryCompanyService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/signup")
	public String signup(@RequestBody @Valid SignUpRequest request, @RequestParam MemberType memberType) {
		if (memberType.equals(MemberType.CONSUMER)) {
			consumerService.signup(request);
		} else if (memberType.equals(MemberType.SELLER)) {
			sellerService.signup(request);
		} else {
			deliveryCompanyService.signup(request);
		}
		return "success signup";
	}
}
