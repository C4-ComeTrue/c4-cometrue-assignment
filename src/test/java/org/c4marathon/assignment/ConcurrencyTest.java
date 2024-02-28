package org.c4marathon.assignment;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.SavingsType;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.ChargeLinkedAccountRepository;
import org.c4marathon.assignment.repository.SavingsAccountRepository;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.ChargeService;
import org.c4marathon.assignment.service.MemberService;
import org.c4marathon.assignment.service.SavingsAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcurrencyTest {

	@Autowired SavingsAccountService savingsAccountService;

	@Autowired AccountService accountService;

	@Autowired MemberService memberService;

	@Autowired ChargeService chargeService;

	@Autowired AccountRepository accountRepository;

	@Autowired SavingsAccountRepository savingsAccountRepository;

	@Autowired ChargeLinkedAccountRepository chargeLinkedAccountRepository;

	@Test
	void 계좌_충전과_적금_이체가_동시에_발생한다면_이체에_실패한다() {
		// given
		// 1. 회원 가입 -> 메인 계좌 자동 생성
		var response = memberService.register("email", "password");
		var memberId = response.memberId();
		var accountId = response.accountId();
		var withdrawAmount = 5000;
		var chargeAmount = 10000;

		// 2. 적금 계좌 생성
		var res = savingsAccountService.createSavingsAccount(memberId, "name", withdrawAmount, SavingsType.REGULAR);
		var savingAccountId = res.id();

		// when
		var future1 = CompletableFuture.runAsync(() ->
		{
			chargeService.charge(accountId, chargeAmount);     // 5000원 충전
		});

		var future2 = CompletableFuture.runAsync(() -> {
			try {
				savingsAccountService.transferForRegularSavings(memberId);  // 5000원 적금 자동 이체
			} catch (BusinessException ex) {
				assertThat(ex.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_LACK_OF_AMOUNT.getMessage());
			}
		});

		CompletableFuture.allOf(future1, future2).join();

		// then
		var accountAmount = accountRepository.findAmount(accountId);
		var savingsAccountEntity = savingsAccountRepository.findById(savingAccountId).orElseThrow();
		assertThat(accountAmount).isEqualTo(chargeAmount);   // 충전은 성공
		assertThat(savingsAccountEntity.getAmount()).isEqualTo(0);  // 적금 이체는 실패
	}

	@Test
	void 동시에_같은_계좌에_송금이_발생한다() throws InterruptedException {
		// given
		// 1. 회원 가입 -> 메인 계좌 자동 생성
		var userA = memberService.register("email1", "password1");
		var userB = memberService.register("email2", "password2");

		var userAAccountId = userA.accountId();
		var userBAccountId = userB.accountId();   // userB 에게 동시에 전송
		var transferAmount = 5000;
		var chargeAmount = 1000000;

		// 2. 각 회원이 10000원씩 충전한다.
		var userBAccountNumber = accountRepository.findById(userBAccountId).orElseThrow().getAccountNumber();
		chargeService.charge(userAAccountId, chargeAmount);

		var concurrentUser = 100;
		var executorService = Executors.newFixedThreadPool(1000);
		var countDownLatch = new CountDownLatch(concurrentUser);

		// when
		for (int i = 0; i < concurrentUser; i++) {
			executorService.execute(() -> {
				accountService.transfer(userAAccountId, userBAccountNumber, transferAmount);
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		executorService.shutdown();

		// then
		var accountEntity = accountRepository.findById(userAAccountId).orElseThrow();
		assertThat(accountEntity.getAmount()).isEqualTo(chargeAmount - transferAmount * concurrentUser);
	}

	@Test
	void 송금_도중_발생한_적금_자동_이체는_실패한다() {
		// given
		// 1. 회원 가입 및 메인 계좌 생성
		var userA = memberService.register("email1", "password1");
		var userB = memberService.register("email2", "password2");

		var userAId = userA.memberId();
		var userAAccountId = userA.accountId();
		var userBAccountNumber = accountRepository.findById(userB.accountId()).orElseThrow().getAccountNumber();

		// 2. 적금 계좌 생성
		var withdrawAmount = 10000;
		var savingsAccountId = savingsAccountService.createSavingsAccount(userAId, "name", withdrawAmount, SavingsType.REGULAR).id();

		// 3. 잔액 충전
		var chargeAmount = 10000;
		chargeService.charge(userAAccountId, chargeAmount);

		// when + then
		var transferAmount = 5000;

		var future1 = CompletableFuture.runAsync(() ->
		{
			try {
				savingsAccountService.transferForRegularSavings(userAId);
			} catch (BusinessException ex) {
				assertThat(ex.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_LACK_OF_AMOUNT.getMessage());
			}
		});

		var future2 = CompletableFuture.runAsync(() ->
			accountService.transfer(userAAccountId, userBAccountNumber, transferAmount)
		);

		CompletableFuture.allOf(future1, future2).join();  // wait

		var transferResultAmount = accountRepository.findAmount(userAAccountId);
		var savingsResultAmount = savingsAccountRepository.findById(savingsAccountId).orElseThrow().getAmount();
		assertThat(transferResultAmount).isEqualTo(chargeAmount - transferAmount);  // 송금 성공
		assertThat(savingsResultAmount).isEqualTo(0);   // 적금 실패
	}

	@Test
	void 송금과_동시에_해당_계좌의_유저가_충전을_수행한다() {
		// given
		var userA = memberService.register("email1", "password1");
		var userB = memberService.register("email2", "password2");

		var userAAccountId = userA.accountId();
		var userBAccountId = userB.accountId();
		var userBAccountNumber = accountRepository.findById(userB.accountId()).orElseThrow().getAccountNumber();

		var transferAmount = 5000;
		var chargeAmount = 5000;

		chargeService.charge(userAAccountId, chargeAmount);

		// when
		var future1 = CompletableFuture.runAsync(() ->
		{
			accountService.transfer(userAAccountId, userBAccountNumber, transferAmount);  // userB로 5000원 송금
		});

		var future2 = CompletableFuture.runAsync(() ->
			chargeService.charge(userBAccountId, chargeAmount)   // 본인 계좌에 5000원 충전
		);

		CompletableFuture.allOf(future1, future2).join();    // wait

		// then
		var resultAmountA = accountRepository.findAmount(userAAccountId);
		var resultAmountB = accountRepository.findAmount(userBAccountId);
		assertThat(resultAmountA).isEqualTo(0);
		assertThat(resultAmountB).isEqualTo(transferAmount + chargeAmount);
	}
}
