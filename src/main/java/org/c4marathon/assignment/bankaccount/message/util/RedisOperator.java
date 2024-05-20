package org.c4marathon.assignment.bankaccount.message.util;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.c4marathon.assignment.bankaccount.service.SendRollbackHandlerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.ByteRecord;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOperator {

	private final RedisTemplate<String, Object> redisTemplate;
	private final SendRollbackHandlerService sendRollbackHandlerService;

	@Value("${redis-stream.pending-time}")
	private int pendingTime;

	public Long ackStream(String streamKey, String consumerGroup, String id) {
		return redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, id);
	}

	/**
	 * 이체한 정보를 기록하기 위해 redis stream에 메시지를 추가하는 메소드
	 *
	 * @param streamKey redis stream 이름(send-stream)
	 * @param sendPk 이체하는 사람의 계좌 pk
	 * @param depositPk 입금받는 사람의 계좌 pk
	 * @param money 이체할 금액
	 */
	public void addStream(String streamKey, long sendPk, long depositPk, long money) {
		RedisConnection connection = getRedisConnection();
		if (connection == null) {
			return;
		}

		RedisAsyncCommands commands = (RedisAsyncCommands)connection.getNativeConnection();

		CommandArgs<String, String> commandArgs = new CommandArgs<>(StringCodec.UTF8).addKey(streamKey)
			.add("*") // id 자동 생성
			.add("send-pk").add(sendPk)
			.add("deposit-pk").add(depositPk)
			.add("money").add(money);

		RedisFuture dispatch = commands.dispatch(CommandType.XADD, new StatusOutput<>(StringCodec.UTF8), commandArgs);
		dispatch.handle((result, exception) -> {
			// 예외가 발생하면 이체 롤백 요청을 보낸다.
			if (exception != null) {
				sendRollbackHandlerService.rollBackDeposit(sendPk, depositPk, money);
			}
			return result;
		});
	}

	public PendingMessages findPendingMessages(String streamKey, String consumerGroupName, String consumerName) {
		return redisTemplate.opsForStream()
			.pending(streamKey, Consumer.from(consumerGroupName, consumerName), Range.unbounded(), 100L);
	}

	/**
	 *
	 * id로 메세지 정보를 얻는 메소드
	 * 0번 인덱스에 send-pk, 1번 인덱스에 deposit-pk, 2번 인덱스에 money를 넣은 Long 타입 배열을 리턴한다.
	 */
	public Long[] findMessageById(String streamKey, String id) {
		RedisStreamCommands command = getRedisConnection();

		// deserialize가 안돼서 직접 타입을 맞춰줌
		// command.range()를 하면 왜인지 모르겠지만 Map형태로 데이터가 나오지 않아서 생기는 문제 같음.
		Long[] values = new Long[3];
		try {
			List<ByteRecord> byteRecords = command.xRange(streamKey.getBytes(StandardCharsets.UTF_8),
				Range.closed(id, id));
			ByteRecord entries = byteRecords.get(0);

			int index = 0;

			for (Map.Entry<byte[], byte[]> entry : entries) {
				String value = new String(entry.getValue(), StandardCharsets.UTF_8);
				values[index++] = Long.valueOf(value);
			}
		} catch (Exception e) {
			// 뭔가 로그 파일로 남기거나 메일로 에러가 발생했다고 알려줘야 할 것 같음
			log.error("[{}] streamKey: {} | id: {} | message: {}", e.getClass().getSimpleName(), streamKey, id,
				e.getMessage());
		}

		return values;
	}

	/**
	 *
	 * pending된 메세지의 consumer name을 바꾸는 메소드
	 * 기존 consumer에서 pending된 메세지를 처리하지 않도록 한다.
	 */
	public void claimMessage(PendingMessage pendingMessage, String streamKey, String consumerName) {
		RedisConnection connection = getRedisConnection();
		if (connection == null) {
			return;
		}

		RedisAsyncCommands commands = (RedisAsyncCommands)connection.getNativeConnection();

		CommandArgs<String, String> commandArgs = new CommandArgs<>(StringCodec.UTF8).addKey(streamKey)
			.add(pendingMessage.getGroupName())
			.add(consumerName)
			.add(pendingTime)
			.add(pendingMessage.getIdAsString());

		commands.dispatch(CommandType.XCLAIM, new StatusOutput<>(StringCodec.UTF8), commandArgs);
	}

	public void createStreamConsumerGroup(String streamKey, String consumerGroupName) {
		// Stream이 존재하지 않는 경우 생성
		if (Boolean.FALSE.equals(redisTemplate.hasKey(streamKey))) {
			RedisConnection connection = getRedisConnection();
			if (connection == null) {
				return;
			}

			RedisAsyncCommands commands = (RedisAsyncCommands)connection.getNativeConnection();

			// 사용할 명령어 생성
			CommandArgs<String, String> commandArgs = new CommandArgs<>(StringCodec.UTF8).add(CommandKeyword.CREATE)
				.add(streamKey) // key
				.add(consumerGroupName) // group
				.add("0") // <id | $> 0을 사용하면 처음부터 전체 스트림을 가져오도록 한다.
				.add("MKSTREAM"); // 스트림이 존재하지 않는 경우 스트림을 0의 길이로 자동 생성

			commands.dispatch(CommandType.XGROUP, new StatusOutput<>(StringCodec.UTF8), commandArgs);

		} else {
			if (!isStreamConsumerGroupExist(streamKey, consumerGroupName)) {
				redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
			}
		}

	}

	/**
	 *
	 * 해당 스트림의 consumer group이 있는지 확인하는 메소드
	 */
	public boolean isStreamConsumerGroupExist(String streamKey, String consumerGroupName) {
		Iterator<StreamInfo.XInfoGroup> iterator = redisTemplate.opsForStream().groups(streamKey).stream().iterator();

		while (iterator.hasNext()) {
			StreamInfo.XInfoGroup xInfoGroup = iterator.next();
			if (xInfoGroup.groupName().equals(consumerGroupName)) {
				return true;
			}
		}
		return false;
	}

	public StreamMessageListenerContainer createStreamMessageListenerContainer() {
		return StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(),
			StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
				.hashKeySerializer(new StringRedisSerializer())
				.hashValueSerializer(new StringRedisSerializer())
				.pollTimeout(Duration.ofMillis(20))
				.build());
	}

	public RedisConnection getRedisConnection() {
		RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
		if (connectionFactory != null) {
			return connectionFactory.getConnection();
		}
		return null;
	}
}
