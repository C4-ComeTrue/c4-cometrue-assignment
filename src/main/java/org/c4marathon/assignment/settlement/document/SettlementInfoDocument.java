package org.c4marathon.assignment.settlement.document;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.ToString;

@Document(collection = "settle_info")
@ToString
public class SettlementInfoDocument {
	@Id
	private ObjectId id;

	private long requestAccountPk; // 정산 요청한 사용자의 pk
	private String requestMemberName; // 정산 요청한 사용자의 이름

	private int totalNumber; // 정산하는 총 사용자의 수

	private long totalMoney; // 정산할 돈

	@Indexed(name = "member_info_index")
	private List<MemberInfoDocument> memberInfoList; // 정산하는 사용자의 정보

	@Indexed(name = "created_date_index")
	LocalDateTime createdAt;

	public SettlementInfoDocument(long requestAccountPk, String requestMemberName, int totalNumber, long totalMoney,
		List<MemberInfoDocument> memberInfoList) {
		this.requestAccountPk = requestAccountPk;
		this.requestMemberName = requestMemberName;
		this.totalNumber = totalNumber;
		this.totalMoney = totalMoney;
		this.memberInfoList = List.copyOf(memberInfoList);
		this.createdAt = LocalDateTime.now().plusHours(9);
	}
}
