package org.c4marathon.assignment.member.dto.response;

public record MemberInfo(
	long memberPk,
	String memberId,
	String memberName
) {
}
