package org.c4marathon.assignment.account.service;

import static org.springframework.http.HttpStatus.*;

import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.TransferToSavingAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.SavingAccount;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.account.repository.SavingAccountRepository;
import org.c4marathon.assignment.auth.service.SecurityService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    private final SavingAccountRepository savingAccountRepository;

    private final MemberService memberService;

    private final SecurityService securityService;

    public static final int DAILY_LIMIT = 3_000_000;

    // 회원 가입 시 메인 계좌 생성
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveMainAccount(Long memberId) {

        Member member = memberService.getMemberById(memberId);
        Account account = createAccount(Type.REGULAR_ACCOUNT, member);

        accountRepository.save(account);
    }

    // 메인 계좌가 존재하는지 확인
    public boolean isMainAccount(Long memberId) {
        return accountRepository.existsAccountByMemberIdAndType(memberId, Type.REGULAR_ACCOUNT);
    }

    // 계좌 객체 생성
    public Account createAccount(Type type, Member member) {

        return Account.builder()
            .type(type)
            .member(member)
            .build();
    }

    // 메인 계좌 조회
    public AccountResponseDto findAccount() {

        // 회원 정보 조회
        Long memberId = securityService.findMember();
        Account account = accountRepository.findByMemberId(memberId);

        // 계좌 조회 후 Entity를 Dto로 변환 후 리턴
        return AccountResponseDto.entityToDto(account);
    }

    // 메인 계좌 잔액 충전
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void rechargeAccount(RechargeAccountRequestDto rechargeAccountRequestDto) {

        // 회원 정보 조회
        Long memberId = securityService.findMember();

        // 계좌 정보 조회
        Account account = accountRepository.findByRegularAccount(memberId)
            .orElseThrow(() -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(),
                FORBIDDEN.toString()));

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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void transferFromRegularAccount(TransferToSavingAccountRequestDto transferToSavingAccountRequestDto) {

        // 회원 정보 조회
        Long memberId = securityService.findMember();

        // 메인 계좌 및 적금 계좌 조회
        Account regularAccount = accountRepository.findByRegularAccount(memberId)
            .orElseThrow(
                () -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));

        // 잔액이 부족하다면 예외 처리
        if (regularAccount.getBalance() < transferToSavingAccountRequestDto.balance()) {
            throw new BaseException(ErrorCode.INSUFFICIENT_BALANCE.toString(), HttpStatus.FORBIDDEN.toString());
        }

        regularAccount.transferBalance(regularAccount.getBalance() - transferToSavingAccountRequestDto.balance());

        SavingAccount savingAccount = savingAccountRepository.findBySavingAccount(memberId,
                transferToSavingAccountRequestDto.receiverAccountId())
            .orElseThrow(() -> new BaseException(ErrorCode.ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));

        savingAccount.transferBalance(savingAccount.getBalance() + transferToSavingAccountRequestDto.balance());

        accountRepository.save(regularAccount);
        savingAccountRepository.save(savingAccount);
    }
}
