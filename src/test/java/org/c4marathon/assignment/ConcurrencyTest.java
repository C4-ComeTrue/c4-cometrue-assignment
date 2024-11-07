package org.c4marathon.assignment;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.SavingsType;
import org.c4marathon.assignment.domain.entity.ChargeLinkedAccount;
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

		// 2. B 계좌로 보낼 수 있도록 잔액을 여유롭게 충전한다.
		var userBAccountNumber = accountRepository.findById(userBAccountId).orElseThrow().getAccountNumber();
		chargeService.charge(userAAccountId, chargeAmount);

		var concurrentUser = 100;
		var executorService = Executors.newFixedThreadPool(1000);
		var countDownLatch = new CountDownLatch(concurrentUser);

		// when
		for (int i = 0; i < concurrentUser; i++) {
			executorService.execute(() -> {
				accountService.transfer(userAAccountId, userBAccountNumber, transferAmount);  // 100명이 userB 계좌로 전송
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		executorService.shutdown();

		// then
		var accountAEntity = accountRepository.findById(userAAccountId).orElseThrow();
		var accountBEntity = accountRepository.findById(userBAccountId).orElseThrow();
		assertThat(accountAEntity.getAmount()).isEqualTo(chargeAmount - transferAmount * concurrentUser);
		assertThat(accountBEntity.getAmount()).isEqualTo(transferAmount * concurrentUser);  // 동시성 이슈 확인 필요
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

	@Test
	void 송금시_자동_충전이_일어날때_내_계좌로_돈이_들어오는_경우_송금이_성공한다() {
		// given
		// 주 계좌 생성
		var userA = memberService.register("email1", "password1");
		var userB = memberService.register("email2", "password2");

		var userAAccountId = userA.accountId();
		var userBAccountId = userB.accountId();
		var userBAccountNumber = accountRepository.findById(userB.accountId()).orElseThrow().getAccountNumber();

		// 유저 A가 주 충전 계좌 생성
		var transferAmount = 5000;
		var chargeAmount = 10000;
		var userAAccount = accountRepository.findById(userAAccountId).orElseThrow();
		var chargedLinkedAccount = ChargeLinkedAccount.builder()
			.account(userAAccount)
			.bank("우리은행")
			.accountNumber("111-0000-222")
			.amount(10000)
			.main(true).build();     // 주 계좌에 10000원 충전 수행

		chargeLinkedAccountRepository.save(chargedLinkedAccount);
		chargeService.charge(userBAccountId, transferAmount);

		// when
		var future1 = CompletableFuture.runAsync(() ->
		{
			accountService.transfer(userAAccountId, userBAccountNumber, transferAmount);  // userA -> B로 5000원 송금 시도 -> 잔액 부족으로 자동 충전 수행
		});

		var future2 = CompletableFuture.runAsync(() ->
		{
			accountService.transfer(userBAccountId, userAAccount.getAccountNumber(), transferAmount);  // userB -> A로 5000원 송금 시도
		});

		// 다수의 비동기 작업을 수행할 때 까지 대기
		CompletableFuture.allOf(future1, future2).join();

		// then
		var resultAmountA = accountRepository.findAmount(userAAccountId);
		var resultAmountB = accountRepository.findAmount(userBAccountId);
		assertThat(resultAmountA).isEqualTo(transferAmount + (chargeAmount - transferAmount));
		assertThat(resultAmountB).isEqualTo(transferAmount);
	}
}
