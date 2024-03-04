package org.c4marathon.assignment.member.session;

import java.io.Serializable;

public record SessionMemberInfo(
	long memberPk,
	String memberId,
	long mainAccountPk
) implements Serializable {
}
