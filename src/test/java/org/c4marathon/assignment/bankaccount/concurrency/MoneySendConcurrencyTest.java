package org.c4marathon.assignment.bankaccount.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.c4marathon.assignment.AssignmentApplication;
import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.limit.ChargeLimitManager;
import org.c4marathon.assignment.bankaccount.limit.LimitConst;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = AssignmentApplication.class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MoneySendConcurrencyTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	MainAccountRepository mainAccountRepository;

	@Autowired
	SavingAccountRepository savingAccountRepository;

	@Autowired
	ChargeLimitManager chargeLimitManager;
	@Autowired
	MainAccountService mainAccountService;

	private Member member;
	private MainAccount mainAccount;
	private SavingAccount savingAccount;
	private long mainAccountPk;
	private long savingAccountPk;

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
			int startMoney = findMainAccount.getMoney();
			int mainPlusMoney = 1000;
			int savingPlusMoney = 1000;
			final int threadCount = 50;
			final ExecutorService executorService = Executors.newFixedThreadPool(25);
			final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failCount = new AtomicInteger();

			// When
			for (int i = 0; i < threadCount; i++) {
				executorService.submit(() -> {
					try {
						mainAccountService.sendToSavingAccount(mainAccountPk, savingAccountPk, savingPlusMoney);
						mainAccountService.chargeMoney(mainAccountPk, mainPlusMoney);
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
		@DisplayName("메인 계좌 잔고가 부족하면 적금 계좌로 송금에 실패한다.")
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
						mainAccountService.sendToSavingAccount(mainAccountPk, savingAccountPk, sendMoney);
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
			assertEquals(10, successCount.get());
			assertEquals(40, failCount.get());
		}
	}

	void createAccount() {
		int money = 100000;
		mainAccount = MainAccount.builder()
			.chargeLimit(LimitConst.CHARGE_LIMIT)
			.money(money)
			.build();

		mainAccountRepository.save(mainAccount);

		member = Member.builder()
			.memberId("testId")
			.password("testPass")
			.memberName("testName")
			.phoneNumber("testPhone")
			.mainAccountPk(mainAccount.getAccountPk())
			.build();
		memberRepository.save(member);

		savingAccount = new SavingAccount();
		savingAccount.init("free", 500);
		savingAccount.addMember(member);
		savingAccountRepository.save(savingAccount);

		mainAccountPk = mainAccount.getAccountPk();
		savingAccountPk = savingAccount.getAccountPk();

		chargeLimitManager.init(mainAccountPk);
	}

	void clearAccount() {
		savingAccountRepository.delete(savingAccount);
		mainAccountRepository.delete(mainAccount);
		memberRepository.delete(member);
	}
}
