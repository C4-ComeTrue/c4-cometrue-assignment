package org.c4marathon.assignment.settlement.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DivideMoneyRequestDto(
	@Max(value = 5, message = "정산은 최대 50명까지 가능합니다.")
	@Min(value = 1, message = "정산은 최소 1명 이상부터 가능합니다.")
	int totalNumber,

	@Positive(message = "정산 금액은 1원 이상부터 가능합니다.")
	long totalMoney,

	@Valid
	@NotNull(message = "계좌 정보를 입력해 주세요.")
	@Size(min = 1, message = "정산은 최소 1명 이상부터 가능합니다.")
	List<MemberInfo> memberInfoList
) {
}
