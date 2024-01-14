package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.account.dto.RequestDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.util.event.MemberJoinedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AccountService implements ApplicationListener<MemberJoinedEvent> {
    
    private final AccountRepository accountRepository;

    private final MemberService memberService;

    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.max-age}")
    private Long expireTimeMs;
    
    // 계좌 생성
    @Transactional
    public void saveAccount(RequestDto.AccountDto accountDto, String token){

        String memberEmail = JwtTokenUtil.getMemberEmail(token, secretKey);
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

    @Override
    public void onApplicationEvent(MemberJoinedEvent event) {

        // 이벤트로부터 회원 이메일 가져오기
        String memberEmail = event.getMemberEmail();
        // 토큰 생성
        String token = JwtTokenUtil.createToken(memberEmail, secretKey, expireTimeMs);
        //계좌 생성
        saveAccount(new RequestDto.AccountDto(Type.REGULAR_ACCOUNT), token);
    }
}
