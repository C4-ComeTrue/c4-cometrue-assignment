package org.c4marathon.assignment.bankaccount.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.message.consumer.RedisStreamConsumer;
import org.c4marathon.assignment.bankaccount.message.util.RedisOperator;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.bankaccount.service.MainAccountService;
import org.c4marathon.assignment.container.ContainerBaseConfig;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MoneySendConcurrencyTest extends ContainerBaseConfig {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	MainAccountRepository mainAccountRepository;

	@Autowired
	SavingAccountRepository savingAccountRepository;

	@Autowired
	MainAccountService mainAccountService;

	@Autowired
	@Qualifier("depositExecutor")
	ThreadPoolTaskExecutor executor;

	@Autowired
	RedisOperator redisOperator;

	@Autowired
	RedisStreamConsumer redisStreamConsumer;

	private Member[] member;
	private MainAccount mainAccount;
	private SavingAccount savingAccount;
	private long[] mainAccountPk;
	private long[] savingAccountPk;

	@Nested
	@DisplayName("메인 계좌에서 적금 계좌 송금시 동시성 테스트")
	class SendToSavingAccount {

		@BeforeEach
		void accountInit() throws IllegalAccessException {
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
			MainAccount findMainAccount = mainAccountRepository.findById(mainAccountPk[0]).get();
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
						mainAccountService.sendToSavingAccount(mainAccountPk[0], savingAccountPk[0], savingPlusMoney);
						mainAccountService.chargeMoney(mainAccountPk[0], mainPlusMoney);
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
			MainAccount resultMainAccount = mainAccountRepository.findById(mainAccountPk[0]).get();
			SavingAccount resultSavingAccount = savingAccountRepository.findById(savingAccountPk[0]).get();

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
						mainAccountService.sendToSavingAccount(mainAccountPk[0], savingAccountPk[0], sendMoney);
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

	@Nested
	@DisplayName("서로 다른 사용자의 메인 계좌 간 이체 동시성 테스트")
	class SendToOtherAccount {

		@BeforeEach
		void accountInit() {
			createAccount();
		}

		@AfterEach
		void accountClear() {
			clearAccount();
		}

		@Test
		@DisplayName("여러 사용자의 계좌 간 동시에 같은 금액의 이체 작업을 진행해도 전체 금액과 개개인의 잔고는 변함이 없어야 한다.")
		void concurrency_send_to_other_account_with_same_condition() throws InterruptedException {
			// Given
			MainAccount mainAccount1 = mainAccountRepository.findById(mainAccountPk[0]).get();
			MainAccount mainAccount2 = mainAccountRepository.findById(mainAccountPk[1]).get();
			MainAccount mainAccount3 = mainAccountRepository.findById(mainAccountPk[2]).get();

			long totalMoney = mainAccount1.getMoney() + mainAccount2.getMoney() + mainAccount3.getMoney();
			long sendMoney = 1000;
			final int threadCount = 10;
			final ExecutorService executorService = Executors.newFixedThreadPool(10);
			final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failCount = new AtomicInteger();

			// When
			for (int i = 0; i < threadCount; i++) {
				executorService.submit(() -> {
					try {
						// j번째 사람은 다음 순서의 사용자 메인 계좌에 이체 작업을 수행한다
						for (int j = 0; j < 3; j++) {
							mainAccountService.sendToOtherAccount(mainAccountPk[j], mainAccountPk[(j + 1) % 3],
								sendMoney);
						}
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

			executor.getThreadPoolExecutor().awaitTermination(5, TimeUnit.SECONDS);

			MainAccount resultMainAccount1 = mainAccountRepository.findById(mainAccountPk[0]).get();
			MainAccount resultMainAccount2 = mainAccountRepository.findById(mainAccountPk[1]).get();
			MainAccount resultMainAccount3 = mainAccountRepository.findById(mainAccountPk[2]).get();
			long resultTotalMoney =
				resultMainAccount1.getMoney() + resultMainAccount2.getMoney() + resultMainAccount3.getMoney();

			assertEquals(totalMoney, resultTotalMoney);
			assertEquals(mainAccount1.getMoney(), resultMainAccount1.getMoney());
			assertEquals(mainAccount2.getMoney(), resultMainAccount2.getMoney());
			assertEquals(mainAccount3.getMoney(), resultMainAccount3.getMoney());
		}

		@Test
		@DisplayName("여러 사용자의 계좌 간 동시에 다른 금액의 이체 작업을 진행해도 전체 금액은 변함이 없어야 한다.")
		void concurrency_send_to_other_account_with_different_condition() throws InterruptedException {
			// Given
			MainAccount mainAccount1 = mainAccountRepository.findById(mainAccountPk[0]).get();
			MainAccount mainAccount2 = mainAccountRepository.findById(mainAccountPk[1]).get();
			MainAccount mainAccount3 = mainAccountRepository.findById(mainAccountPk[2]).get();

			long totalMoney = mainAccount1.getMoney() + mainAccount2.getMoney() + mainAccount3.getMoney();
			long[] sendMoney = {1000, 2000, 3000};
			final int threadCount = 10;
			final ExecutorService executorService = Executors.newFixedThreadPool(10);
			final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failCount = new AtomicInteger();

			// When
			for (int i = 0; i < threadCount; i++) {
				executorService.submit(() -> {
					try {
						// j번째 사람은 다음 순서의 사용자 메인 계좌에 이체 작업을 수행한다
						for (int j = 0; j < 3; j++) {
							mainAccountService.sendToOtherAccount(mainAccountPk[j], mainAccountPk[(j + 1) % 3],
								sendMoney[j]);
						}
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

			executor.getThreadPoolExecutor().awaitTermination(5, TimeUnit.SECONDS);

			MainAccount resultMainAccount1 = mainAccountRepository.findById(mainAccountPk[0]).get();
			MainAccount resultMainAccount2 = mainAccountRepository.findById(mainAccountPk[1]).get();
			MainAccount resultMainAccount3 = mainAccountRepository.findById(mainAccountPk[2]).get();
			long resultTotalMoney =
				resultMainAccount1.getMoney() + resultMainAccount2.getMoney() + resultMainAccount3.getMoney();

			assertEquals(totalMoney, resultTotalMoney);
			assertEquals(mainAccount1.getMoney() - 1000 * 10 + 3000 * 10, resultMainAccount1.getMoney());
			assertEquals(mainAccount2.getMoney() - 2000 * 10 + 1000 * 10, resultMainAccount2.getMoney());
			assertEquals(mainAccount3.getMoney() - 3000 * 10 + 2000 * 10, resultMainAccount3.getMoney());
		}

		@Test
		@DisplayName("한 사람에게 여러 사람들이 동시에 이체를 해도 전체 금액은 변함이 없어야 한다.")
		void concurrency_send_to_one_account_with_different_condition() throws InterruptedException {
			// Given
			MainAccount mainAccount1 = mainAccountRepository.findById(mainAccountPk[0]).get();
			MainAccount mainAccount2 = mainAccountRepository.findById(mainAccountPk[1]).get();
			MainAccount mainAccount3 = mainAccountRepository.findById(mainAccountPk[2]).get();

			long totalMoney = mainAccount1.getMoney() + mainAccount2.getMoney() + mainAccount3.getMoney();
			long[] sendMoney = {1000, 2000};
			final int threadCount = 10;
			final ExecutorService executorService = Executors.newFixedThreadPool(10);
			final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failCount = new AtomicInteger();

			// When
			for (int i = 0; i < threadCount; i++) {
				executorService.submit(() -> {
					try {
						for (int j = 0; j < 2; j++) {
							mainAccountService.sendToOtherAccount(mainAccountPk[j], mainAccountPk[2],
								sendMoney[j]);
						}
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

			executor.getThreadPoolExecutor().awaitTermination(5, TimeUnit.SECONDS);

			MainAccount resultMainAccount1 = mainAccountRepository.findById(mainAccountPk[0]).get();
			MainAccount resultMainAccount2 = mainAccountRepository.findById(mainAccountPk[1]).get();
			MainAccount resultMainAccount3 = mainAccountRepository.findById(mainAccountPk[2]).get();

			long resultTotalMoney =
				resultMainAccount1.getMoney() + resultMainAccount2.getMoney() + resultMainAccount3.getMoney();

			assertEquals(totalMoney, resultTotalMoney);
			assertEquals(mainAccount1.getMoney() - 1000 * 10, resultMainAccount1.getMoney());
			assertEquals(mainAccount2.getMoney() - 2000 * 10, resultMainAccount2.getMoney());
			assertEquals(mainAccount3.getMoney() + 3000 * 10, resultMainAccount3.getMoney());
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
