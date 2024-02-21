package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;
import static org.springframework.http.HttpStatus.*;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestInstance(value = PER_CLASS)
@ActiveProfiles("test")
public class AccountServiceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    private Account account;

    // 회원가입과 기본 계좌 생성
    @BeforeAll
    void setUp() {
        member = createMember();
        account = createAccount(Type.REGULAR_ACCOUNT, member);
        account.transferBalance(10000L);
        account.resetDailyLimit(10000);
        memberRepository.save(member);
        accountRepository.save(account);
    }

    @AfterAll
    void tearDown() {
        accountRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    // 계좌 객체 생성
    private Account createAccount(Type type, Member member) {

        return Account.builder()
            .type(type)
            .member(member)
            .build();
    }

    private Member createMember() {

        return Member.builder()
            .email("test@naver.com")
            .password("test")
            .name("test")
            .build();
    }

    @Nested
    @DisplayName("계좌 생성 테스트")
    class Create {

        @DisplayName("메인 계좌가 존재한다.")
        @Test
        void existsAccount() {

            // when
            boolean existsMainAccount = accountRepository.existsAccountByMemberIdAndType(member.getId(),
                Type.REGULAR_ACCOUNT);

            // then
            assertTrue(existsMainAccount);
        }

        // 메인 계좌가 존재하는지 확인
        private boolean isMainAccount(Long memberId) {
            return accountRepository.existsAccountByMemberIdAndType(memberId, Type.REGULAR_ACCOUNT);
        }

        @DisplayName("계좌 생성 요청이 들어오면 요청에 따른 타입의 계좌를 생성하고 메인 계좌가 존재하지 않는다면 생성한다.")
        @Test
        @Transactional
        void createAccountTest() {

            // given
            Account account1 = createAccount(Type.ADDITIONAL_ACCOUNT, member);

            // when
            accountRepository.save(account1);
            if (!isMainAccount(member.getId())) {
                accountRepository.save(createAccount(Type.REGULAR_ACCOUNT, member));
            }

            // then
            Optional<Account> findAccount = accountRepository.findById(account1.getId());
            assertThat(findAccount).isPresent();
            assertThat(findAccount.get().getId()).isEqualTo(account1.getId());
        }
    }

    @Nested
    @DisplayName("계좌 조회 테스트")
    class Read {

        @DisplayName("사용자의 생성된 모든 계좌를 불러온다.")
        @Test
        void findAccountTest() {

            // when
            List<Account> accountList = accountRepository.findByMemberId(member.getId());
            // 계좌 조회 후 Entity를 Dto로 변환 후 리턴
            List<AccountResponseDto> accountResponseDtoList = accountList.stream()
                .map(AccountResponseDto::entityToDto)
                .toList();

            // then
            assertThat(accountList).isNotNull();
            assertEquals(accountList.get(0).getId(), accountResponseDtoList.get(0).id());
            assertEquals(accountList.get(0).getBalance(), accountResponseDtoList.get(0).balance());
            assertEquals(accountList.get(0).getDailyLimit(), accountResponseDtoList.get(0).dailyLimit());
            assertEquals(accountList.get(0).getType(), accountResponseDtoList.get(0).type());
        }

        @DisplayName("사용자의 특정 계좌를 불러온다.")
        @Test
        @Transactional
        void findMemberAccountTest() {

            // when
            Account account1 = accountRepository.findByAccount(member.getId(), account.getId())
                .orElseThrow(
                    () -> new BaseException(ErrorCode.ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));

            AccountResponseDto accountResponseDto = AccountResponseDto.entityToDto(account1);

            // then
            assertEquals(account1.getId(), account.getId());
            assertEquals(account1.getId(), accountResponseDto.id());
            assertEquals(account1.getType(), accountResponseDto.type());
            assertEquals(account1.getBalance(), accountResponseDto.balance());
            assertEquals(account1.getDailyLimit(), accountResponseDto.dailyLimit());
        }
    }

    @Nested
    @DisplayName("계좌 충전 테스트")
    class Recharge {

        public static final int DAILY_LIMIT = 3_000_000;

        @DisplayName("사용자의 외부 계좌에서 메인 계좌로 10,000원을 이체한다.")
        @Test
        @Transactional
        void transferToRegularAccountTest() {

            // given
            // 이체 금액
            Long afterBalance = 10000L;

            // when
            Account account1 = accountRepository.findByRegularAccount(member.getId()).orElseThrow(
                () -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));
            Integer dailyLimit = account1.getDailyLimit() + afterBalance.intValue();
            Long balance = account1.getBalance() + afterBalance;
            // 하루 충전 금액이 300만원 보다 적어야 함.
            if (dailyLimit > DAILY_LIMIT) {
                throw new BaseException(ErrorCode.EXCEEDED_DAILY_LIMIT.toString(), HttpStatus.BAD_REQUEST.toString());
            }
            account1.resetDailyLimit(dailyLimit);
            account1.transferBalance(balance);
            accountRepository.save(account1);

            Account resultAccount = accountRepository.findByRegularAccount(member.getId()).orElseThrow(
                () -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));

            // then
            assertThat(resultAccount.getBalance()).isEqualTo(balance);
            assertThat(resultAccount.getDailyLimit()).isEqualTo(dailyLimit);
        }

        @DisplayName("메인 계좌에 잔액 충전 요청 시 충전 한도가 넘어 오류가 발생한다.")
        @Test
        @Transactional
        void rechargeAccountErrorTest() {

            // given
            // 이체 금액
            long afterBalance = Integer.toUnsignedLong(DAILY_LIMIT);

            // when
            Account account1 = accountRepository.findByRegularAccount(member.getId()).orElseThrow(
                () -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));

            // 하루 충전 금액이 300만원 보다 적어야 함.
            Exception exception = assertThrows(BaseException.class, () -> {
                if (account1.getDailyLimit() + (int)afterBalance > DAILY_LIMIT) {
                    throw new BaseException(ErrorCode.EXCEEDED_DAILY_LIMIT.toString(),
                        HttpStatus.BAD_REQUEST.toString());
                }
            });

            // then
            assertEquals(HttpStatus.BAD_REQUEST.toString(), exception.getMessage());
        }

        @DisplayName("메인 계좌에서 적금 계좌로 이체 요청 시 입력한 금액만큼 충전된다.")
        @Test
        @Transactional
        void transferFromRegularAccountTest() {

            // given
            // 적금 계좌로 이체하려는 금액
            Long balance = 10000L;
            // 적금 계좌
            Account savingAccount = createAccount(Type.INSTALLMENT_SAVINGS_ACCOUNT, member);
            accountRepository.save(savingAccount);

            // when
            // 비관적 락을 걸어두어 행단위 잠금이 되었고, 해당 트랜잭션 안에서만 조회가 가능.

            Account afterSavingAccount = accountRepository.findByAccount(member.getId(), savingAccount.getId())
                .orElseThrow(
                    () -> new BaseException(ErrorCode.ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));
            Account afterRegularAccount = accountRepository.findByRegularAccount(member.getId()).orElseThrow(
                () -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));
            if (afterRegularAccount.getBalance() < balance) {
                throw new BaseException(ErrorCode.INSUFFICIENT_BALANCE.toString(), HttpStatus.FORBIDDEN.toString());
            }
            afterRegularAccount.transferBalance(afterRegularAccount.getBalance() - balance);
            afterSavingAccount.transferBalance(afterSavingAccount.getBalance() + balance);
            accountRepository.saveAll(List.of(afterRegularAccount, afterSavingAccount));

            Account resultSavingAccount = accountRepository.findByAccount(member.getId(), afterSavingAccount.getId())
                .orElseThrow(
                    () -> new BaseException(ErrorCode.ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));
            Account resultRegularAccount = accountRepository.findByRegularAccount(member.getId()).orElseThrow(
                () -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));

            // then
            assertThat(resultRegularAccount.getBalance()).isEqualTo(afterRegularAccount.getBalance());
            assertThat(resultSavingAccount.getBalance()).isEqualTo(afterSavingAccount.getBalance());
        }
    }

    @DisplayName("메인 계좌에서 적금 계좌로 입금 시 잔액 부족하다면 오류가 발생한다.")
    @Test
    @Transactional
    void transferFromRegularAccountErrorTest() {

        // given
        // 적금 계좌로 이체하려는 금액
        Long balance = 50000L;

        // when
        Account afterRegularAccount = accountRepository.findByRegularAccount(member.getId()).orElseThrow(
            () -> new BaseException(ErrorCode.REGULAR_ACCOUNT_DOES_NOT_EXIST.toString(), FORBIDDEN.toString()));
        Exception exception = assertThrows(BaseException.class, () -> {
            if (afterRegularAccount.getBalance() < balance) {
                throw new BaseException(ErrorCode.INSUFFICIENT_BALANCE.toString(), HttpStatus.FORBIDDEN.toString());
            }
        });

        // then
        assertEquals(HttpStatus.FORBIDDEN.toString(), exception.getMessage());
    }
}