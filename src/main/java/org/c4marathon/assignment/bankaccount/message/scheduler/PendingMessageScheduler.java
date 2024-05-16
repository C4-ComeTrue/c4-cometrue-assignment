package org.c4marathon.assignment.bankaccount.message.scheduler;

import java.util.Arrays;

import org.c4marathon.assignment.bankaccount.message.util.RedisOperator;
import org.c4marathon.assignment.bankaccount.service.DepositHandlerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PendingMessageScheduler {
	@Value("${redis-stream.stream-key}")
	private String streamKey;
	@Value("${redis-stream.consumer-group-name}")
	private String consumerGroupName;
	@Value("${redis-stream.consumer-name}")
	private String consumerName;
	@Value("${redis-stream.claim-consumer-name}")
	private String claimConsumerName; // xclaim에 사용할 새로운 소비자 이름
	private final RedisOperator redisOperator;
	private final DepositHandlerService depositHandlerService;

	/**
	 *
	 * 어떠한 문제로 처리되지 않은 메세지를 처리하는 메소드
	 * 기존 consumer가 중복하여 처리하지 않도록 consumer-name을 변경
	 * 이후 doDeposit 메소드로
	 */
	@Scheduled(fixedRate = 5000)
	public void consumePendingMessage() {
		PendingMessages pendingMessages = redisOperator.findPendingMessages(streamKey, consumerGroupName, consumerName);
		for (PendingMessage pendingMessage : pendingMessages) {
			// 기존 consumer가 처리하지 못하도록 consumer name을 claim-consumer로 변경한다.
			redisOperator.claimMessage(pendingMessage, streamKey, claimConsumerName);

			// streamKey와 id에 해당하는 sned-pk, deposit-pk, money를 차례대로 담은 Long 배열을 조회한다.
			Long[] data = redisOperator.findMessageById(streamKey,
				pendingMessage.getIdAsString());

			System.out.println("pending data = " + Arrays.toString(data));

			// 처리되지 않은 이체 로그는 롤백을 시켜준다.
			if (data != null) {
				// 롤백을 하므로 send-pk에 money를 추가해준다.
				depositHandlerService.doDeposit(data[0], data[2], pendingMessage.getIdAsString());
			}
		}
	}

	/**
	 *
	 * consumePendingMessage()에서 오류가 발생하여 처리하지 못한 메세지를 처리하는 메소드
	 * consumePendingMessage()와 유사하지만 바뀐 claim-consumer에 대해서만 처리한다.
	 */
	@Scheduled(fixedRate = 10000)
	public void consumeClaimMessage() {
		PendingMessages pendingMessages = redisOperator.findPendingMessages(streamKey, consumerGroupName,
			claimConsumerName);
		for (PendingMessage pendingMessage : pendingMessages) {
			Long[] data = redisOperator.findMessageById(streamKey,
				pendingMessage.getIdAsString());

			if (data != null) {
				depositHandlerService.doDeposit(data[0], data[2], pendingMessage.getIdAsString());
			}
		}
	}
}
