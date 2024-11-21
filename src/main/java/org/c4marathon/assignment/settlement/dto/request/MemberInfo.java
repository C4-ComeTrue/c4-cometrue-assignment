package org.c4marathon.assignment.settlement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record MemberInfo(
	@Positive(message = "정산할 사용자의 올바른 계좌 정보를 입력해 주세요.")
	long accountPk,
	@NotBlank(message = "정산할 사용자의 이름을 입력해 주세요.")
	String memberName
) {
}
