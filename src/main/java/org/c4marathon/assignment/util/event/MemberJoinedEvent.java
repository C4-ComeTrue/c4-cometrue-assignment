package org.c4marathon.assignment.util.event;

import org.springframework.context.ApplicationEvent;

public class MemberJoinedEvent extends ApplicationEvent {
    private Long memberId;

    public MemberJoinedEvent(Object source, Long memberId) {
        super(source);
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }
}