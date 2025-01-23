package org.c4marathon.global.session;

import java.io.Serializable;

public record SessionMemberInfo(
    Long memberId,
    String email

) implements Serializable {
}
