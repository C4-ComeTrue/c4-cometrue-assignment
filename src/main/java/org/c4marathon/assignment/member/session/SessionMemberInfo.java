package org.c4marathon.assignment.member.session;

public record SessionMemberInfo(
	long memberPk,
	String memberId,
	long mainAccountPk
) {
}
