package org.c4marathon.assignment.account.presentation;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.global.annotation.Login;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/charge")
    public ResponseEntity<Void> charge(
            @Login SessionMemberInfo loginMember,
            @RequestParam @PositiveOrZero long money
    ) {
        accountService.chargeMoney(loginMember.accountId(), money);
        return ResponseEntity.ok().build();
    }
}
