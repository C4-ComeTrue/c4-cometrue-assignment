package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.account.dto.RequestDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AccountService {
    
    private final AccountRepository accountRepository;

    private final MemberService memberService;

    @Value("${jwt.key}")
    private String secretKey;
    
    // 계좌 생성
    @Transactional
    public void saveAccount(RequestDto.AccountDto accountDto){

        String memberEmail = JwtTokenUtil.getMemberEmail(accountDto.token(), secretKey);
        Account account = createAccount(accountDto.type(), memberService.getMemberByEmail(memberEmail));

        accountRepository.save(account);
    }

    // 계좌 객체 생성
    private Account createAccount(Type type, Member member) {

        return Account.builder()
            .type(type)
            .member(member)
            .build();
    }
}
