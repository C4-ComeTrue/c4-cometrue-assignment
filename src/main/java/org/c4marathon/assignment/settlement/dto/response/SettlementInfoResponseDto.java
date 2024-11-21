package org.c4marathon.assignment.settlement.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.c4marathon.assignment.settlement.document.MemberInfoDocument;
import org.c4marathon.assignment.settlement.document.SettlementInfoDocument;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record SettlementInfoResponseDto(
	@JsonSerialize(using = ToStringSerializer.class)
	ObjectId id,
	long requestAccountPk,
	String requestMemberName,
	int totalNumber,
	long totalMoney,
	List<MemberInfoDocument> memberInfoList,
	LocalDateTime createdAt
) {
	public SettlementInfoResponseDto(SettlementInfoDocument document) {
		this(document.getId(), document.getRequestAccountPk(), document.getRequestMemberName(),
			document.getTotalNumber(),
			document.getTotalMoney(), List.copyOf(document.getMemberInfoList()), document.getCreatedAt());
	}
}
