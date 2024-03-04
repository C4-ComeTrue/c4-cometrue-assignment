package org.c4marathon.assignment.account.service;

import java.util.List;

import org.c4marathon.assignment.account.dto.request.AccountRequestDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.SavingAccount;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.SavingAccountRepository;
import org.c4marathon.assignment.auth.service.SecurityService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingAccountService {

    private final SecurityService securityService;

    private final MemberService memberService;

    private final AccountService accountService;

    private final SavingAccountRepository savingAccountRepository;

    public SavingAccount createSavingAccount(Type type, Member member) {
        return SavingAccount.builder()
            .type(type)
            .member(member)
            .build();
    }

    // 적금 계좌 생성
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveSavingAccount(AccountRequestDto accountRequestDto) {

        Long memberId = securityService.findMember();
        Member member = memberService.getMemberById(memberId);
        SavingAccount savingAccount = createSavingAccount(accountRequestDto.type(), member);
        savingAccountRepository.save(savingAccount);

        // 메인 계좌가 존재하지 않을 시 확인 후 생성
        if (!accountService.isMainAccount(memberId)) {
            accountService.saveMainAccount(memberId);
        }
    }

    // 사용자의 전체 적금 계좌 조회
    public List<SavingAccountResponseDto> findSavingAccount() {

        Long memberId = securityService.findMember();
        List<SavingAccount> savingAccounts = savingAccountRepository.findByMemberId(memberId);
        return savingAccounts.stream()
            .map(SavingAccountResponseDto::entityToDto)
            .toList();
    }
}
