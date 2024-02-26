package org.c4marathon.assignment.util.event;

import org.c4marathon.assignment.account.service.AccountService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventListener {

    private final AccountService accountService;

    // 회원가입 시 계좌를 생성하는 이벤트 수신
    @org.springframework.context.event.EventListener
    public void memberJoinedEvent(MemberJoinedEvent event) {

        // 계좌 생성
        accountService.saveMainAccount(event.getMemberId());
    }
}
