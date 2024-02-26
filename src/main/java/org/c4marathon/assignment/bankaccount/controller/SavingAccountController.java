package org.c4marathon.assignment.bankaccount.controller;

import java.util.List;

import org.c4marathon.assignment.bankaccount.dto.request.CreateSavingAccountRequestDto;
import org.c4marathon.assignment.bankaccount.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.bankaccount.dto.response.SavingProductResponseDto;
import org.c4marathon.assignment.bankaccount.product.ProductManager;
import org.c4marathon.assignment.bankaccount.service.SavingAccountService;
import org.c4marathon.assignment.common.annotation.Login;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts/saving")
public class SavingAccountController {

	private final SavingAccountService savingAccountService;
	private final ProductManager productManager;

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/products")
	List<SavingProductResponseDto> getProductInfo(@Login SessionMemberInfo memberInfo) {
		return productManager.getProductInfo();
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	void create(@Login SessionMemberInfo memberInfo,
		@Valid @RequestBody CreateSavingAccountRequestDto requestDto) {
		savingAccountService.create(memberInfo.memberPk(), requestDto.productName());
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping
	List<SavingAccountResponseDto> getSavingAccountInfo(@Login SessionMemberInfo memberInfo) {
		return savingAccountService.getSavingAccountInfo(memberInfo.memberPk());
	}
}
