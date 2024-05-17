package org.c4marathon.assignment.bankaccount.message;

import java.util.concurrent.TimeUnit;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
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
		void accountInit() {
			createAccount();
			createPendingMessage();
		}

		// @BeforeEach()
		// void initPendingMessage() {
		// 	createPendingMessage();
		// }

		@AfterEach
		void accountClear() {
			clearAccount();
		}

		int money = 1000;

		@Test
		@DisplayName("Pending된 메세지가 있으면 롤백한다")
		void pending_message_will_rollback() throws InterruptedException {
			// Given
			MainAccount sendAccount = mainAccountRepository.findById(mainAccountPk[0]).get();
			long originMoney = sendAccount.getMoney();

			PendingMessages pendingMessages = redisOperator.findPendingMessages(streamKey, consumerGroup,
				claimConsumerName);
			for (PendingMessage pendingMessage : pendingMessages) {
				redisOperator.claimMessage(pendingMessage, streamKey, consumerName); // 원래 consumer name으로 변경
			}

			// When
			executor.initialize();
			pendingMessageScheduler.consumePendingMessage();
			executor.getThreadPoolExecutor().awaitTermination(5, TimeUnit.SECONDS);

			// Then
			MainAccount rollBackMember = mainAccountRepository.findById(mainAccountPk[0]).get();
			assertEquals(rollBackMember.getMoney() - originMoney, money);
		}

		@Test
		@DisplayName("처리하지 못한 pending 메세지가 있으면 롤백한다.")
		void not_consumed_pending_message_will_rollback() throws InterruptedException {
			// Given
			MainAccount sendAccount = mainAccountRepository.findById(mainAccountPk[0]).get();
			long originMoney = sendAccount.getMoney();

			// When
			executor.initialize();
			pendingMessageScheduler.consumeClaimMessage();
			executor.getThreadPoolExecutor().awaitTermination(10, TimeUnit.SECONDS);

			// Then
			MainAccount rollBackMember = mainAccountRepository.findById(mainAccountPk[0]).get();
			assertEquals(rollBackMember.getMoney() - originMoney, money);
		}

	}

	void createPendingMessage() {
		// 레디스에 값 넣자마자 스레드 풀이 동작하기 때문에 잠시 꺼두고 ack하기 전에 redis의 메세지 pending 처리
		executor.shutdown();
		redisOperator.addStream(streamKey, mainAccountPk[0], mainAccountPk[1], 1000);
		PendingMessages pendingMessages = redisOperator.findPendingMessages(streamKey, consumerGroup, consumerName);
		for (PendingMessage pendingMessage : pendingMessages) {
			redisOperator.claimMessage(pendingMessage, streamKey, claimConsumerName);
		}
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
