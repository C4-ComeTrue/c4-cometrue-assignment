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

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
    private final AccountRepository accountRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);

    @Scheduled(fixedRate = 10000)
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
}
