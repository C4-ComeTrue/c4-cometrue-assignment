package org.c4marathon.assignment.member.dto.response;

public record MemberInfoResponseDto(
	long memberPk,
	String memberId,
	String memberName
) {
}
