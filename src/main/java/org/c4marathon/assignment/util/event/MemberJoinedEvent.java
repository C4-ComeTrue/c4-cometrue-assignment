package org.c4marathon.assignment.util.event;

import org.springframework.context.ApplicationEvent;

public class MemberJoinedEvent extends ApplicationEvent {
    private String memberEmail;

    public MemberJoinedEvent(Object source, String memberEmail) {
        super(source);
        this.memberEmail = memberEmail;
    }

    public String getMemberEmail() {
        return memberEmail;
    }
}