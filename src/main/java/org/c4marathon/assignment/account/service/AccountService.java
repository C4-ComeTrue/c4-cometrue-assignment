package org.c4marathon.assignment.account.service;

import static org.springframework.http.HttpStatus.*;

import java.util.List;

import org.c4marathon.assignment.account.dto.request.AccountRequestDto;
import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.SavingAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.util.event.MemberJoinedEvent;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService implements ApplicationListener<MemberJoinedEvent> {
    private final AccountRepository accountRepository;

    private final MemberService memberService;

    public static final int DAILY_LIMIT = 3_000_000;

    private Long findMember() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return (Long)authentication.getPrincipal();
    }

    // 계좌 생성
    @Transactional
    public void saveAccount(AccountRequestDto accountRequestDto) {

        Long memberId = findMember();
        Member member = memberService.getMemberById(memberId);
        Account account = createAccount(accountRequestDto.type(), member);

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

        // 계좌 생성
        saveAccount(new AccountRequestDto(Type.REGULAR_ACCOUNT));
    }

    // 계좌 전체 조회
    public List<AccountResponseDto> findAccount() {

        // 회원 정보 조회
        Long memberId = findMember();
        List<Account> accountList = accountRepository.findByMemberId(memberId);

        // 계좌 조회 후 Entity를 Dto로 변환 후 리턴
        return accountList.stream()
            .map(AccountResponseDto::entityToDto)
            .toList();
    }

    // 메인 계좌 잔액 충전
    @Transactional
    public void rechargeAccount(RechargeAccountRequestDto rechargeAccountRequestDto) {

        // 회원 정보 조회
        Long memberId = findMember();

        // 계좌 정보 조회
        Account account = accountRepository.findByRegularAccount(memberId)
            .orElseGet(() -> {
                // 메인 계좌가 존재하지 않는다면
                // 새로운 메인 계좌 생성 후 반환
                saveAccount(new AccountRequestDto(Type.REGULAR_ACCOUNT));
                return accountRepository.findByRegularAccount(memberId)
                    .orElseThrow(() -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(),
                        FORBIDDEN.toString()));
            });
        int dailyLimit = account.getDailyLimit() + rechargeAccountRequestDto.balance().intValue();
        Long balance = account.getBalance() + rechargeAccountRequestDto.balance();

        // 충전 한도 확인
        if (dailyLimit > DAILY_LIMIT) {
            throw new BaseException(ErrorCode.EXCEEDED_DAILY_LIMIT.toString(), HttpStatus.BAD_REQUEST.toString());
        }

        // 충전
        account.resetDailyLimit(dailyLimit);
        account.transferBalance(balance);

        accountRepository.save(account);
    }

    // 메인 계좌에서 적금 계좌로 이체
    @Transactional
    public void transferFromRegularAccount(SavingAccountRequestDto savingAccountRequestDto) {

        // 회원 정보 조회
        Long memberId = findMember();

        // 메인 계좌 및 적금 계좌 조회
        Account regularAccount = accountRepository.findByRegularAccount(memberId)
            .orElseThrow(
                () -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));
        Account savingAccount = accountRepository.findByAccount(memberId,
                savingAccountRequestDto.receiverAccountId())
            .orElseThrow(() -> new BaseException(ErrorCode.ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));

        // 잔액이 부족하다면 예외 처리
        if (regularAccount.getBalance() < savingAccountRequestDto.balance()) {
            throw new BaseException(ErrorCode.INSUFFICIENT_BALANCE.toString(), HttpStatus.FORBIDDEN.toString());
        }

        // 적금 이체
        regularAccount.transferBalance(regularAccount.getBalance() - savingAccountRequestDto.balance());
        savingAccount.transferBalance(savingAccount.getBalance() + savingAccountRequestDto.balance());

        accountRepository.saveAll(List.of(regularAccount, savingAccount));
    }
}
