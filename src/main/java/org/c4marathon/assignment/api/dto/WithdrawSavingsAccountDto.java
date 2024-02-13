package org.c4marathon.assignment.api.dto;

public class WithdrawSavingsAccountDto {

	public record Res(
		long mainAccountAmount,
		long savingsAccountAmount
	) {
	}
}
