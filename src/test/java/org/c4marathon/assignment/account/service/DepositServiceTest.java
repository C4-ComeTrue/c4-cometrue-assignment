package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private DepositService depositService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @DisplayName("Redis에 입금 대기 데이터가 존재할 경우, 모든 입금 요청을 처리하고 Redis에서 삭제한다.")
    @Test
    void deposit_Success() throws Exception {
        // given
        List<String> deposits = List.of(
                "tx1:1:2:1000",
                "tx2:2:3:3000",
                "tx3:3:4:4000"
        );
        given(listOperations.range("pending-deposits", 0, -1)).willReturn(deposits);

        Account account1 = mock(Account.class);
        Account account2 = mock(Account.class);
        Account account3 = mock(Account.class);

        given(accountRepository.findByIdWithLock(2L)).willReturn(Optional.of(account1));
        given(accountRepository.findByIdWithLock(3L)).willReturn(Optional.of(account2));
        given(accountRepository.findByIdWithLock(4L)).willReturn(Optional.of(account3));

        // when
        depositService.deposits();

        // then
        verify(listOperations, times(3)).remove(eq("pending-deposits"), anyLong(), anyString());
        verify(accountRepository, times(3)).save(any(Account.class));

        verify(account1).deposit(1000);
        verify(account2).deposit(3000);
        verify(account3).deposit(4000);
    }


    @DisplayName("Redis에 입금 대기 데이터가 없는 경우, 입금 처리를 하지 않는다.")
    @Test
    void deposits_ShouldNotProcess_WhenNoPendingDepositsExist() throws Exception {

        // given
        given(listOperations.range("pending-deposits", 0, -1)).willReturn(List.of());

        // when
        depositService.deposits();

        // then
        verify(listOperations, times(1)).range("pending-deposits", 0, -1);
        verify(listOperations, never()).remove(eq("pending-deposits"), anyLong(), anyString());
    }

    @DisplayName("입금 실패 시 failed-deposits로 데이터가 저장된다.")
    @Test
    void deposits_Failure() throws Exception {
        // given
        String deposit = "tx1:1:2:1000";
        given(listOperations.range("pending-deposits", 0, -1)).willReturn(List.of(deposit));
        given(accountRepository.findByIdWithLock(2L)).willThrow(new NotFoundAccountException());

        // when
        depositService.deposits();

        // then
        verify(listOperations).remove(eq("pending-deposits"), eq(1L), eq(deposit));
        verify(listOperations).rightPush("failed-deposits", deposit);
    }

    @DisplayName("실패한 입금들은 재시도를 한다.")
    @Test
    void rollbackDeposits_RetrySuccess() throws Exception {
        // given
        String failedDeposit = "tx1:1:2:1000";
        given(listOperations.range("failed-deposits", 0, -1))
                .willReturn(List.of(failedDeposit));

        Account receiverAccount = mock(Account.class);
        given(accountRepository.findByIdWithLock(2L))
                .willReturn(Optional.of(receiverAccount));

        // when
        depositService.rollbackDeposits();

        // then
        verify(listOperations).remove(eq("failed-deposits"), eq(1L), eq(failedDeposit));
        verify(receiverAccount).deposit(1000);
        verify(accountRepository).save(receiverAccount);
    }

    @DisplayName("최대 재시도 횟수 초과 시 출금을 롤백 한다.")
    @Test
    void rollbackDeposits_ExceedMaxRetries() throws Exception {

        // given
        String failedDeposit = "tx1:1:2:1000";
        given(listOperations.range("failed-deposits", 0, -1))
                .willReturn(List.of(failedDeposit));

        given(accountRepository.findByIdWithLock(2L))
                .willThrow(new NotFoundAccountException());

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment("deposit-failures:tx1"))
                .willReturn(6L);

        Account senderAccount = mock(Account.class);
        given(accountRepository.findByIdWithLock(1L))
                .willReturn(Optional.of(senderAccount));
        // when
        depositService.rollbackDeposits();
        // then
        verify(listOperations).remove(eq("failed-deposits"), eq(1L), eq(failedDeposit));
        verify(redisTemplate).delete("deposit-failures:tx1");
        verify(senderAccount).deposit(1000);
        verify(accountRepository).save(senderAccount);
    }
}
