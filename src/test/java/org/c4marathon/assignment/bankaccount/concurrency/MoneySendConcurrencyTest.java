package org.c4marathon.assignment.bankaccount.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.c4marathon.assignment.bankaccount.entity.ChargeLimit;
import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.repository.ChargeLimitRepository;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.bankaccount.service.MainAccountService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class MoneySendConcurrencyTest {

	private static final String REDIS_IMAGE = "redis:latest";
	private static final int REDIS_PORT = 6379;

	@Container
	private static RedisContainer redis = new RedisContainer(DockerImageName.parse(REDIS_IMAGE)).withExposedPorts(
		REDIS_PORT);

	@DynamicPropertySource
	private static void redisProperties(DynamicPropertyRegistry registry) {
		redis.start();
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", redis::getFirstMappedPort);
	}

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	MainAccountRepository mainAccountRepository;

	@Autowired
	SavingAccountRepository savingAccountRepository;

	@Autowired
	ChargeLimitRepository chargeLimitRepository;
	@Autowired
	MainAccountService mainAccountService;
	@Autowired
	RedisTemplate redisTemplate;

	private Member member;
	private MainAccount mainAccount;
	private SavingAccount savingAccount;
	private ChargeLimit chargeLimit;
	private long mainAccountPk;
	private long savingAccountPk;
	private long chargeLimitPk;

	@Nested
	@DisplayName("메인 계좌에서 적금 계좌 송금시 동시성 테스트")
	class SendToSavingAccount {

		@BeforeEach
		void accountInit() {
			createAccount();
		}

		@AfterEach
		void accountClear() {
			clearAccount();
		}

		@Test
		@DisplayName("메인 계좌에서 적금 계좌로 송금하는 작업과 내 계좌로 입금되는 작업이 동시에 일어나도 총 돈의 액수는 변함없어야 한다.")
		void concurrency_send_to_saving_account_and_my_account() throws InterruptedException {
			// Given
			MainAccount findMainAccount = mainAccountRepository.findById(mainAccountPk).get();
			long startMoney = findMainAccount.getMoney();
			long mainPlusMoney = 1000;
			long savingPlusMoney = 1000;
			final int threadCount = 50;
			final ExecutorService executorService = Executors.newFixedThreadPool(25);
			final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failCount = new AtomicInteger();

			// When
			for (int i = 0; i < threadCount; i++) {
				executorService.submit(() -> {
					try {
						mainAccountService.sendToSavingAccount(mainAccountPk, savingAccountPk, savingPlusMoney,
							chargeLimitPk);
						mainAccountService.chargeMoney(mainAccountPk, mainPlusMoney, chargeLimitPk);
						successCount.getAndIncrement();
					} catch (Exception exception) {
						failCount.getAndIncrement();
						exception.printStackTrace();
					} finally {
						countDownLatch.countDown();
					}
				});
			}

			countDownLatch.await();
			executorService.shutdown();

			// then
			MainAccount resultMainAccount = mainAccountRepository.findById(mainAccountPk).get();
			SavingAccount resultSavingAccount = savingAccountRepository.findById(savingAccountPk).get();

			assertEquals(startMoney, resultMainAccount.getMoney()); // 충전과 송금 금액이 같으니 메인 계좌는 처음 조회 했을 때 값과 일치해야 한다.
			assertEquals(savingPlusMoney * threadCount,
				resultSavingAccount.getSavingMoney()); // 적금 계좌는 5000*10만큼 있어야 한다.
			assertEquals(threadCount, successCount.get());
			assertEquals(0, failCount.get());
		}

		@Test
		@DisplayName("메인 계좌 잔고가 부족해도 충전 한도 안이면 자동 충전되어 송금에 성공한다.")
		void concurrency_send_to_saving_account() throws InterruptedException {
			int threadCount = 50;
			int sendMoney = 10000;
			final ExecutorService executorService = Executors.newFixedThreadPool(25);
			final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failCount = new AtomicInteger();

			// When
			for (int i = 0; i < threadCount; i++) {
				executorService.submit(() -> {
					try {
						mainAccountService.sendToSavingAccount(mainAccountPk, savingAccountPk, sendMoney,
							chargeLimitPk);
						successCount.getAndIncrement();
					} catch (Exception exception) {
						failCount.getAndIncrement();
					} finally {
						countDownLatch.countDown();
					}
				});
			}

			countDownLatch.await();
			executorService.shutdown();

			// Then
			assertEquals(threadCount, successCount.get());
		}
	}

	void createAccount() {
		int money = 100000;
		mainAccount = new MainAccount(money);
		mainAccountRepository.save(mainAccount);

		chargeLimit = new ChargeLimit();
		chargeLimitRepository.save(chargeLimit);

		member = Member.builder()
			.memberId("testId")
			.password("testPass")
			.memberName("testName")
			.phoneNumber("testPhone")
			.mainAccountPk(mainAccount.getAccountPk())
			.chargeLimitPk(chargeLimit.getLimitPk())
			.build();
		memberRepository.save(member);

		savingAccount = new SavingAccount("free", 500);
		savingAccount.addMember(member);
		savingAccountRepository.save(savingAccount);

		mainAccountPk = mainAccount.getAccountPk();
		savingAccountPk = savingAccount.getAccountPk();
		chargeLimitPk = chargeLimit.getLimitPk();
	}

	void clearAccount() {
		savingAccountRepository.delete(savingAccount);
		mainAccountRepository.delete(mainAccount);
		memberRepository.delete(member);
		chargeLimitRepository.delete(chargeLimit);
	}
}
