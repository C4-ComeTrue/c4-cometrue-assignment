package org.c4marathon.assignment.dto.response;

import java.util.Collections;
import java.util.List;

public record SettlementInfoRes(List<SettlementDetailInfoRes> details) {
	public SettlementInfoRes(List<SettlementDetailInfoRes> details) {
		this.details = Collections.unmodifiableList(details);
	}
}
