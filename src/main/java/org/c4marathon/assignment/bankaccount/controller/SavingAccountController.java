package org.c4marathon.assignment.bankaccount.controller;

import java.util.Map;

import org.c4marathon.assignment.bankaccount.product.ProductManager;
import org.c4marathon.assignment.bankaccount.service.SavingAccountService;
import org.c4marathon.assignment.common.annotation.Login;
import org.c4marathon.assignment.common.session.SessionMemberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
	Map<String, Integer> getProductInfo(@Login SessionMemberInfo memberInfo) {
		return productManager.getProductInfo();
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/{productNumber}")
	void create(@Login SessionMemberInfo memberInfo,
		String productName) {
		savingAccountService.create(memberInfo.memberPk(), productName);
	}

}
