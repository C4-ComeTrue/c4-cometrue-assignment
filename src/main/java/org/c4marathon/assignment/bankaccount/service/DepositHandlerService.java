package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.message.util.RedisOperator;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositHandlerService {
	@Value("${redis-stream.stream-key}")
	private String streamKey;
	@Value("${redis-stream.consumer-group-name}")
	private String consumerGroup;
	private final MainAccountRepository mainAccountRepository;
	private final RedisOperator redisOperator;

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
	@Async("depositExecutor")
	public void doDeposit(long accountPk, long money, String streamId) {
		mainAccountRepository.deposit(accountPk, money);
		redisOperator.ackStream(streamKey, consumerGroup, streamId);
	}
}
