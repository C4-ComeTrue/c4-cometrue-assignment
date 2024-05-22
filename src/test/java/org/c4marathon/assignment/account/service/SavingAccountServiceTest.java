package org.c4marathon.assignment.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.c4marathon.assignment.account.dto.request.AccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.entity.SavingAccount;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.SavingAccountRepository;
import org.c4marathon.assignment.auth.service.SecurityService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SavingAccountServiceTest {

    @InjectMocks
    private SavingAccountService savingAccountService;

    @Mock
    private SecurityService securityService;

    @Mock
    private MemberService memberService;

    @Mock
    private SavingAccountRepository savingAccountRepository;

    @Mock
    private AccountService accountService;

    @Nested
    @DisplayName("적금 계좌 생성 테스트")
    class Create {
        @DisplayName("적금 계좌를 생성한다.")
        @Test
        void createMainAccountTest() {
            // given
            Long memberId = 0L;
            Member member = mock(Member.class);
            SavingAccount savingAccount = mock(SavingAccount.class);
            AccountRequestDto accountRequestDto = new AccountRequestDto(Type.INSTALLMENT_SAVINGS_ACCOUNT);

            given(securityService.findMember()).willReturn(memberId);
            given(memberService.getMemberById(memberId)).willReturn(member);
            given(savingAccountRepository.save(any(SavingAccount.class))).willReturn(savingAccount);
            given(accountService.isMainAccount(memberId)).willReturn(true);

            // when
            savingAccountService.saveSavingAccount(accountRequestDto);

            // then
            then(securityService).should(times(1)).findMember();
            then(memberService).should(times(1)).getMemberById(memberId);
            then(savingAccountRepository).should(times(1)).save(any(SavingAccount.class));
            then(accountService).should(times(1)).isMainAccount(memberId);
        }

        @DisplayName("적금 계좌를 생성할 때 메인 계좌가 없다면 함께 생성해준다.")
        @Test
        void createMainAccountAndMainAccountTest() {
            // given
            Long memberId = 0L;
            Member member = mock(Member.class);
            SavingAccount savingAccount = mock(SavingAccount.class);
            AccountRequestDto accountRequestDto = new AccountRequestDto(Type.INSTALLMENT_SAVINGS_ACCOUNT);

            given(securityService.findMember()).willReturn(memberId);
            given(memberService.getMemberById(memberId)).willReturn(member);
            given(savingAccountRepository.save(any(SavingAccount.class))).willReturn(savingAccount);
            given(accountService.isMainAccount(memberId)).willReturn(false);
            willDoNothing().given(accountService).saveMainAccount(memberId);

            // when
            savingAccountService.saveSavingAccount(accountRequestDto);

            // then
            then(securityService).should(times(1)).findMember();
            then(memberService).should(times(1)).getMemberById(memberId);
            then(savingAccountRepository).should(times(1)).save(any(SavingAccount.class));
            then(accountService).should(times(1)).isMainAccount(memberId);
            then(accountService).should(times(1)).saveMainAccount(memberId);
        }
    }

    @Nested
    @DisplayName("계좌 조회 테스트")
    class Read {
        @DisplayName("계좌 조회를 위해 회원의 정보를 조회하고, 회원의 메인 계좌 정보를 불러온다.")
        @Test
        void findSavingAccountTest() {
            // given
            Long memberId = 0L;
            SavingAccount savingAccount = mock(SavingAccount.class);
            AccountResponseDto accountResponseDto = mock(AccountResponseDto.class);
            given(securityService.findMember()).willReturn(memberId);
            given(savingAccountRepository.findByMemberId(memberId)).willReturn(List.of(savingAccount));

            // when
            List<SavingAccountResponseDto> savingAccountResponseDtoList = savingAccountService.findSavingAccount();

            // then
            then(securityService).should(times(1)).findMember();
            then(savingAccountRepository).should(times(1)).findByMemberId(memberId);
            assertEquals(savingAccountResponseDtoList.get(0).id(), accountResponseDto.id());
        }
    }
}
