package org.c4marathon.assignment.account.service;

import java.util.List;

import org.c4marathon.assignment.account.dto.RequestDto;
import org.c4marathon.assignment.account.dto.ResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.util.event.MemberJoinedEvent;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
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
    public void saveAccount(RequestDto.AccountDto accountDto, String token) {

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

    // 회원가입 시 계좌를 생성하는 이벤트 수신
    @Override
    public void onApplicationEvent(MemberJoinedEvent event) {

        // 이벤트로부터 회원 이메일 가져오기
        String memberEmail = event.getMemberEmail();
        // 토큰 생성
        String token = JwtTokenUtil.createToken(memberEmail, secretKey, expireTimeMs);
        // 계좌 생성
        saveAccount(new RequestDto.AccountDto(Type.REGULAR_ACCOUNT), token);
    }

    // 계좌 전체 조회
    public List<ResponseDto.AccountDto> findAccount(String token) {

        // 회원 정보 조회
        String memberEmail = JwtTokenUtil.getMemberEmail(token, secretKey);
        Member member = memberService.getMemberByEmail(memberEmail);

        List<Account> accountList = accountRepository.findByMember(member);

        // 계좌 조회 후 Entity를 Dto로 변환 후 리턴
        return accountList.stream()
            .map(ResponseDto.AccountDto::entityToDto)
            .toList();
    }

    // 메인 계좌 잔액 충전
    @Transactional
    public void rechargeAccount(RequestDto.RechargeAccountDto rechargeAccountDto, String token) {

        // 회원 정보 조회
        String memberEmail = JwtTokenUtil.getMemberEmail(token, secretKey);
        Member member = memberService.getMemberByEmail(memberEmail);

        // 계좌 정보 조회
        Account account = accountRepository.findByAccount(member.getId(), rechargeAccountDto.accountId());
        int dailyLimit = account.getDailyLimit() + rechargeAccountDto.balance();
        int balance = account.getBalance() + rechargeAccountDto.balance();

        // 충전 한도 확인
        if (dailyLimit > 3000000) {
            throw new BaseException(ErrorCode.EXCEEDED_DAILY_LIMIT.toString(), HttpStatus.BAD_REQUEST.toString());
        }

        // 충전
        account.resetDailyLimit(dailyLimit);
        account.transferBalance(balance);

        accountRepository.save(account);
    }

    // 메인 계좌에서 적금 계좌로 이체
    // 없는 계좌는 조회가 안 되기에 예외 처리 x
    @Transactional
    public void transferFromRegularAccount(RequestDto.SavingAccountDto savingAccountDto, String token) {

        // 회원 정보 조회
        String memberEmail = JwtTokenUtil.getMemberEmail(token, secretKey);
        Member member = memberService.getMemberByEmail(memberEmail);

        // 메인 계좌 및 적금 계좌 조회
        Account regularAccount = accountRepository.findByRegularAccount(member.getId());
        Account savingAccount = accountRepository.findByAccount(member.getId(), savingAccountDto.receiverAccountId());

        // 잔액이 부족하다면 예외 처리
        if (regularAccount.getBalance() < savingAccountDto.balance()) {
            throw new BaseException(ErrorCode.INSUFFICIENT_BALANCE.toString(), HttpStatus.FORBIDDEN.toString());
        }

        // 적금 이체
        regularAccount.transferBalance(regularAccount.getBalance() - savingAccountDto.balance());
        savingAccount.transferBalance(savingAccount.getBalance() + savingAccountDto.balance());

        accountRepository.saveAll(List.of(regularAccount, savingAccount));
    }
}
