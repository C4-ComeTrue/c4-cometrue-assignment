package org.c4marathon.assignment.bankaccount.message.consumer;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.c4marathon.assignment.bankaccount.message.util.RedisOperator;
import org.c4marathon.assignment.bankaccount.service.DepositHandlerService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, Object, String>>, InitializingBean,
	DisposableBean {

	private StreamMessageListenerContainer<String, MapRecord<String, Object, String>> listenerContainer;
	private Subscription subscription;

	@Value("${redis-stream.stream-key}")
	private String streamKey;
	@Value("${redis-stream.consumer-group-name}")
	private String consumerGroupName;
	@Value("${redis-stream.consumer-name}")
	private String consumerName;
	@Value("${redis-stream.is-test}")
	private boolean isTest;

	private final RedisOperator redisOperator;

	private final DepositHandlerService depositHandlerService;

	@Override
	public void destroy() {
		if (subscription != null) {
			subscription.cancel();
		}
		if (listenerContainer != null) {
			listenerContainer.stop();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Consumer Group 초기화
		redisOperator.createStreamConsumerGroup(streamKey, consumerGroupName);

		// StreamMessageListenerContainer 설정
		listenerContainer = redisOperator.createStreamMessageListenerContainer();

		subscription = listenerContainer.receive(
			Consumer.from(consumerGroupName, consumerName),
			StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
			this
		);

		subscription.await(Duration.ofSeconds(2));

		listenerContainer.start();
	}

	/**
	 *
	 * Redis Stream 메세지를 처리하는 메소드
	 * XADD된 메세지를 먼저 처리한다. 처리와 동시에 consumer-group에 pending 한다.
	 */
	@Override
	public void onMessage(MapRecord<String, Object, String> message) {
		// 테스트에서 ack 처리 되지 않도록 처리
		if (isTest) {
			return;
		}
		Set<Map.Entry<Object, String>> entries = message.getValue().entrySet();
		Long[] accountData = new Long[3];
		int index = 0;

		// depositData에 send-pk, deposit-pk, money를 차례대로 담는다.
		for (Map.Entry<Object, String> entry : entries) {
			String value = entry.getValue();
			accountData[index++] = Long.valueOf(value);
		}

		// deposit-pk에 money를 추가하는 task 추가
		Long ackResult = redisOperator.ackStream(streamKey, consumerGroupName, message.getId().toString());
		if (ackResult != 0) {
			depositHandlerService.doDeposit(accountData[0], accountData[1], accountData[2]);
		}
	}
}
