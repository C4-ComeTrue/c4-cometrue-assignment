package org.c4marathon.assignment.member.session;

import lombok.Builder;

@Builder
public record SessionMemberInfo(
	long memberPk,
	String memberId,
	long mainAccountPk
) {
}
