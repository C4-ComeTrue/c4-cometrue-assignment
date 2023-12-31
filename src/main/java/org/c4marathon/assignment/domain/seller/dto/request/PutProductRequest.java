package org.c4marathon.assignment.domain.seller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PutProductRequest {

	@Size(max = 100, message = "name length exceed 100")
	@NotNull(message = "name is null")
	private String name;
	@Size(max = 500, message = "description length exceed 500")
	@NotNull(message = "description is null")
	private String description;
	@NotNull(message = "amount is null")
	private Long amount;
	@NotNull(message = "stock is null")
	private Integer stock;
}
