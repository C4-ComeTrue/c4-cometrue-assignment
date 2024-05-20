package org.c4marathon.assignment.bankaccount.async.message;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.message.consumer.RedisStreamConsumer;
import org.c4marathon.assignment.bankaccount.message.scheduler.PendingMessageScheduler;
import org.c4marathon.assignment.bankaccount.message.util.RedisOperator;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class PendingMessageTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	MainAccountRepository mainAccountRepository;

	@Autowired
	SavingAccountRepository savingAccountRepository;

	@Autowired
	@Qualifier("depositExecutor")
	ThreadPoolTaskExecutor executor;

	@Autowired
	RedisOperator redisOperator;

	@Autowired
	PendingMessageScheduler pendingMessageScheduler;

	@Autowired
	RedisStreamConsumer redisStreamConsumer;
	@Autowired
	RedisTemplate redisTemplate;

	private Member[] member;
	private MainAccount mainAccount;
	private SavingAccount savingAccount;
	private long[] mainAccountPk;
	private long[] savingAccountPk;

	@Value("${redis-stream.stream-key}")
	private String streamKey;
	@Value("${redis-stream.consumer-group-name}")
	private String consumerGroup;
	@Value("${redis-stream.consumer-name}")
	private String consumerName;
	@Value("${redis-stream.claim-consumer-name}")
	private String claimConsumerName;

	@Nested
	@DisplayName("Pending 메세지 테스트")
	class SendToSavingAccount {

		@BeforeEach
		void init() throws IllegalAccessException {
			createAccount();
			FieldUtils.writeField(redisStreamConsumer, "isTest", true, true);
		}

		@AfterEach
		void accountClear() throws IllegalAccessException {
			clearAccount();
			FieldUtils.writeField(redisStreamConsumer, "isTest", false, true);
		}

		int money = 1000;

		@Test
		@DisplayName("Pending된 메세지가 있으면 롤백한다")
		void pending_message_will_rollback() throws InterruptedException {
			// Give
			MainAccount sendAccount = mainAccountRepository.findById(mainAccountPk[0]).get();
			long originMoney = sendAccount.getMoney();
			makePendingMessage();

			// When
			executor.initialize();
			executor.getThreadPoolExecutor().awaitTermination(3, TimeUnit.SECONDS);
			pendingMessageScheduler.consumePendingMessage();
			executor.getThreadPoolExecutor().awaitTermination(2, TimeUnit.SECONDS);

			// Then
			MainAccount rollBackMember = mainAccountRepository.findById(mainAccountPk[0]).get();
			assertEquals(rollBackMember.getMoney() - originMoney, money);
		}

		@Test
		@DisplayName("Pending된 메세지가 pending time이 지나지 않았다면 처리하지 않는다.")
		void if_pending_message_under_pending_time_will_break() throws InterruptedException {
			// Given
			MainAccount sendAccount = mainAccountRepository.findById(mainAccountPk[0]).get();
			long originMoney = sendAccount.getMoney();
			makePendingMessage();

			// When
			executor.initialize();
			pendingMessageScheduler.consumePendingMessage();

			// Then
			PendingMessages pendingMessages = redisOperator.findPendingMessages(streamKey, consumerGroup, consumerName);
			assertEquals(false, pendingMessages.isEmpty());
		}

		@Test
		@DisplayName("처리하지 못한 pending 메세지가 있으면 롤백한다.")
		void not_consumed_pending_message_will_rollback() throws InterruptedException {
			// Given
			MainAccount sendAccount = mainAccountRepository.findById(mainAccountPk[0]).get();
			long originMoney = sendAccount.getMoney();
			makePendingMessage();
			// given(redisOperator.findMessageById(streamKey,).findByPkForUpdate(anyLong())).willReturn(Optional.of(mainAccount));

			// claim처리 안된 메세지 claim 처리
			Awaitility.await().atLeast(3, TimeUnit.SECONDS).until(() -> {
				PendingMessages pendingMessages = redisOperator.findPendingMessages(streamKey, consumerGroup,
					consumerName);
				if (pendingMessages.isEmpty()) {
					return true;
				}

				for (PendingMessage pendingMessage : pendingMessages) {
					redisOperator.claimMessage(pendingMessage, streamKey, claimConsumerName);
				}

				return false;
			});

			// When
			executor.initialize();
			pendingMessageScheduler.consumeClaimMessage();
			executor.getThreadPoolExecutor().awaitTermination(2, TimeUnit.SECONDS);

			// Then
			MainAccount rollBackMember = mainAccountRepository.findById(mainAccountPk[0]).get();
			assertEquals(rollBackMember.getMoney() - originMoney, money);
		}

	}

	void makePendingMessage() {
		executor.shutdown();
		redisOperator.addStream(streamKey, mainAccountPk[0], mainAccountPk[1], 1000);
		// 이벤트 발생으로 스레드 풀에 들어간 작업 제거
		executor.getThreadPoolExecutor().remove(() -> {

		});
	}

	void createAccount() {
		mainAccountPk = new long[3];
		savingAccountPk = new long[3];
		member = new Member[3];
		for (int i = 0; i < 3; i++) {
			int money = 100000;
			mainAccount = new MainAccount();
			mainAccount.charge(money);
			mainAccountRepository.save(mainAccount);

			member[i] = Member.builder()
				.memberId("testId" + i)
				.password("testPass" + i)
				.memberName("testName" + i)
				.phoneNumber("testPhone" + i)
				.mainAccountPk(mainAccount.getAccountPk())
				.build();
			memberRepository.save(member[i]);

			savingAccount = new SavingAccount("free", 500);
			savingAccount.addMember(member[i]);
			savingAccountRepository.save(savingAccount);

			mainAccountPk[i] = mainAccount.getAccountPk();
			savingAccountPk[i] = savingAccount.getAccountPk();
		}
	}

	void clearAccount() {
		for (int i = 0; i < 3; i++) {
			savingAccount = savingAccountRepository.findById(savingAccountPk[i]).get();
			mainAccount = mainAccountRepository.findById(mainAccountPk[i]).get();
			savingAccountRepository.delete(savingAccount);
			mainAccountRepository.delete(mainAccount);
			memberRepository.delete(member[i]);
		}
	}
}
