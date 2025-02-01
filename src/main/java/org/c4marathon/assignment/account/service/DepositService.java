package org.c4marathon.assignment.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.c4marathon.assignment.global.util.Const.MAX_RETRIES;


@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
    private final AccountRepository accountRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);


    @Scheduled(fixedRate = 10000)
    public void deposits() {
        threadPoolExecutor.init();

        List<String> deposits = redisTemplate.opsForList().range("pending-deposits", 0, -1);
        if (deposits == null || deposits.isEmpty()) {
            return;
        }

        for (String deposit : deposits) {
            threadPoolExecutor.execute(() -> processDeposit(deposit));
        }

        try {
            threadPoolExecutor.waitToEnd();
        } catch (Exception e) {
            log.error("스레드 풀 실행 중 예외 발생 : {}", e.getMessage(), e);
        }
    }


    /**
     * 입금 실패한 경우가 많이 없을 것이라고 생각하여 멀티 스레드 X
     *  나중에 멀티 스레드 성능 테스트 후 결정
     */
    @Scheduled(fixedRate = 12000)
    public void rollbackDeposits() {
        List<String> failedDeposits = redisTemplate.opsForList().range("failed-deposits", 0, -1);
        if (failedDeposits == null || failedDeposits.isEmpty()) {
            return;
        }

        for (String depositRequest : failedDeposits) {
            processFailedDeposit(depositRequest);
        }
    }

    private void processDeposit(String deposit) {
        String[] parts = deposit.split(":");
        String transactionId = parts[0];
        Long senderAccountId = Long.valueOf(parts[1]);
        Long receiverAccountId = Long.valueOf(parts[2]);
        long money = Long.parseLong(parts[3]);

        try {
            redisTemplate.opsForList().remove("pending-deposits", 1, deposit);

            Account receiverAccount = accountRepository.findByIdWithLock(receiverAccountId)
                    .orElseThrow(NotFoundAccountException::new);

            receiverAccount.deposit(money);
            accountRepository.save(receiverAccount);

            log.debug("입금 성공: transactionId={}, senderAccountId={}, receiverAccountId={}, money={}",
                    transactionId, senderAccountId, receiverAccountId, money);
        } catch (Exception e) {
            redisTemplate.opsForList().rightPush("failed-deposits", deposit);
            log.debug("입금 실패: transactionId={}, senderAccountId={}, receiverAccountId={}, money={}",
                    transactionId, senderAccountId, receiverAccountId, money);

        }
    }

    private void processFailedDeposit(String failedDeposit) {
        String[] parts = failedDeposit.split(":");
        String transactionId = parts[0];
        Long senderAccountId = Long.valueOf(parts[1]);
        Long receiverAccountId = Long.valueOf(parts[2]);
        long money = Long.parseLong(parts[3]);

        try {
            Account receiverAccount = accountRepository.findByIdWithLock(receiverAccountId)
                    .orElseThrow(NotFoundAccountException::new);

            receiverAccount.deposit(money);
            accountRepository.save(receiverAccount);
            redisTemplate.opsForList().remove("failed-deposits", 1, failedDeposit);
            log.debug("입금 재시도 성공: transactionId={}, senderAccountId={}, receiverAccountId={}, money={}",
                    transactionId, senderAccountId, receiverAccountId, money);
        } catch (Exception e) {
            log.debug("입금 재시도 실패: transactionId={}, senderAccountId={}, receiverAccountId={}, money={}",
                    transactionId, senderAccountId, receiverAccountId, money);

            int retryCount = redisTemplate.opsForValue().increment("deposit-failures:" + transactionId).intValue();

            if (retryCount > MAX_RETRIES) {
                log.error("입금 실패 횟수 초과, 출금 롤백 진행: transactionId={}, senderId={}, money={}", transactionId, senderAccountId, money);

                redisTemplate.opsForList().remove("failed-deposits", 1, failedDeposit);
                rollbackWithdraw(senderAccountId, money);

                redisTemplate.delete("deposit-failures:" + transactionId);
            }
        }
    }

    private void rollbackWithdraw(Long senderAccountId, long money) {
        Account senderAccount = accountRepository.findByIdWithLock(senderAccountId)
                .orElseThrow(NotFoundAccountException::new);

        senderAccount.deposit(money);
        accountRepository.save(senderAccount);
    }



  /*  @Scheduled(fixedRate = 10000)
    public void deposits() {
        threadPoolExecutor.init();

        Map<Object, Object> deposits = redisTemplate.opsForHash().entries("pending-deposits");
        if (deposits.isEmpty()) return;

        for (Map.Entry<Object, Object> entry : deposits.entrySet()) {
            threadPoolExecutor.execute(() -> {

                Long receiverId = Long.valueOf(entry.getKey().toString());
                long totalAmount = Long.parseLong(entry.getValue().toString());

                try {
                    Account receiverAccount = accountRepository.findByIdWithLock(receiverId)
                            .orElseThrow(NotFoundAccountException::new);

                    receiverAccount.deposit(totalAmount);
                    accountRepository.save(receiverAccount);

                    redisTemplate.opsForHash().delete("pending-deposits", receiverId);
                } catch (Exception e) {
                    log.error("입금 처리 실패 : receiverId = {}, amount = {}", receiverId, totalAmount);
                    redisTemplate.opsForHash().increment("failed-deposits", receiverId, totalAmount);
                }
            });
        }

        try {
            threadPoolExecutor.waitToEnd();
        } catch (Exception e) {
            log.error("스레드 풀 실행 중 예외 발생 : {}", e.getMessage(), e);
        }
    }

    *//**
     * 입금 실패한 경우가 많이 없을 것이라고 생각하여 멀티 스레드 X
     * 나중에 멀티 스레드 성능 테스트 후 결정
     * 서버에 문제가 있어 아무리 재시도 해도 입금이 안되는 경우는 어떻게 처리해야할지 고민
     *//*
    @Scheduled(fixedRate = 12000)
    public void rollbackDeposits() {
        threadPoolExecutor.init();

        Map<Object, Object> failedDeposits = redisTemplate.opsForHash().entries("failed-deposits");

        if (failedDeposits.isEmpty()) {
            return;
        }

        for (Map.Entry<Object, Object> entry : failedDeposits.entrySet()) {
            Long receiverId = Long.valueOf(entry.getKey().toString());
            long totalAmount = Long.parseLong(entry.getValue().toString());

            try {
                Account receiverAccount = accountRepository.findByIdWithLock(receiverId)
                        .orElseThrow(NotFoundAccountException::new);

                receiverAccount.deposit(totalAmount);
                accountRepository.save(receiverAccount);

                redisTemplate.opsForHash().delete("failed-deposits", receiverId);
            } catch (Exception e) {
                log.error("입금 처리 실패 : receiverId = {}, amount = {}", receiverId, totalAmount);
            }
        }

    }*/

}
