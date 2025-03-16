package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.global.util.Const.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transaction.domain.TransactionType.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.dto.WithdrawRequest;
import org.c4marathon.assignment.account.exception.DailyChargeLimitExceededException;
import org.c4marathon.assignment.account.service.query.AccountQueryService;
import org.c4marathon.assignment.account.service.query.SavingAccountQueryService;
import org.c4marathon.assignment.global.event.transactional.TransactionCreateEvent;
import org.c4marathon.assignment.global.util.AccountNumberUtil;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.c4marathon.assignment.transaction.exception.InvalidTransactionStatusException;
import org.c4marathon.assignment.transaction.exception.UnauthorizedTransactionException;
import org.c4marathon.assignment.transaction.service.TransactionQueryService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;
	private final MemberRepository memberRepository;
	private final TransactionRepository transactionRepository;
	private final AccountQueryService accountQueryService;
	private final SavingAccountQueryService savingAccountQueryService;
	private final TransactionQueryService transactionQueryService;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public void createAccount(Long memberId) {
		String accountNumber = AccountNumberUtil.generateAccountNumber(ACCOUNT_PREFIX);

		Account account = Account.create(accountNumber, DEFAULT_BALANCE);
		accountRepository.save(account);

		Member member = memberRepository.findById(memberId)
			.orElseThrow(NotFoundMemberException::new);
		member.setMainAccountNumber(account.getAccountNumber());

		memberRepository.save(member);
	}

	/**
	 * 메인 계좌에 돈을 충전하다.
	 * 한 번에 메인 계좌에다가 충전을 여러 번 할 수도 있다. 어떻게 관리해야하나?
	 * @param accountNumber
	 * @param money
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void chargeMoney(String accountNumber, long money) {
		Account account = accountQueryService.findAccountWithLock(accountNumber);

		if (!account.isChargeWithinDailyLimit(money)) {
			throw new DailyChargeLimitExceededException();
		}

		account.deposit(money);
		accountRepository.save(account);
	}

	/**
	 * 적금 계좌로 송금하는 로직
	 * 자유 적금일 때 사용
	 * @param accountNumber
	 * @param savingAccountNumber
	 * @param money
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendToSavingAccount(String accountNumber, String savingAccountNumber, long money) {
		Account account = accountQueryService.findAccountWithLock(accountNumber);
		SavingAccount savingAccount = savingAccountQueryService.findFreeSavingAccountWithLock(savingAccountNumber);

		if (!account.isSend(money)) {
			autoCharge(money, account);
		}

		account.withdraw(money);
		savingAccount.deposit(money);

		Transaction transaction = Transaction.create(
			account.getAccountNumber(),
			savingAccountNumber,
			money,
			IMMEDIATE_TRANSFER,
			SUCCESS_DEPOSIT,
			LocalDateTime.now()
		);

		transactionRepository.save(transaction);

	}

	/**
	 * 송금 시 송금 내역을 저장하는 이벤트 발행 후 커밋
	 * @param senderAccountNumber
	 * @param request
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void withdraw(String senderAccountNumber, WithdrawRequest request) {
		Account senderAccount = accountQueryService.findAccountWithLock(senderAccountNumber);

		validateReceiverAccountNumber(request.receiverAccountNumber());

		if (!senderAccount.isSend(request.money())) {
			autoCharge(request.money(), senderAccount);
		}

		senderAccount.withdraw(request.money());
		accountRepository.save(senderAccount);

		if (request.type().equals(IMMEDIATE_TRANSFER)) {
			eventPublisher.publishEvent(
				new TransactionCreateEvent(
					senderAccountNumber,
					request.receiverAccountNumber(),
					request.money(),
					request.type(),
					WITHDRAW,
					LocalDateTime.now()
				)
			);
		} else if (request.type().equals(PENDING_TRANSFER)) {
			eventPublisher.publishEvent(
				new TransactionCreateEvent(
					senderAccountNumber,
					request.receiverAccountNumber(),
					request.money(),
					request.type(),
					PENDING_DEPOSIT,
					LocalDateTime.now()
				)
			);
		}
	}

	/**
	 * 송금 취소 기능(사용자가 직접 취소 요청)
	 * 취소하려는 송금 내역을 가져와 검증 후 송금을 취소함
	 * partitionSendTime 포함해야함
	 * @param senderAccountNumber
	 * @param transactionalId
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void cancelWithdraw(String senderAccountNumber, Long transactionalId, LocalDateTime sendTime) {

		Transaction transaction = transactionQueryService.findTransactionByIdWithLock(transactionalId, sendTime);

		validateTransactional(senderAccountNumber, transaction);

		Account senderAccount = accountQueryService.findAccountWithLock(senderAccountNumber);

		senderAccount.deposit(transaction.getAmount());
		transaction.updateStatus(CANCEL);
	}

	/**
	 * 72시간이 지난 송금 내역을 취소하는 비즈니스 로직
	 *
	 * @param transaction
	 */
	public void cancelWithdrawByExpirationTime(Transaction transaction) {
		Account senderAccount = accountQueryService.findAccountWithLock(transaction.getSenderAccountNumber());

		senderAccount.deposit(transaction.getAmount());
		transaction.updateStatus(CANCEL);
	}

	/**
	 * 입금 재시도가 실패하면 송금 롤백을 하는 로직
	 *
	 * @param senderAccountNumber
	 * @param money
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void rollbackWithdraw(String senderAccountNumber, long money) {
		Account senderAccount = accountQueryService.findAccountWithLock(senderAccountNumber);

		senderAccount.deposit(money);
		accountRepository.save(senderAccount);
	}

	/**
	 * 송금할 때 메인 계좌에 잔액이 부족할 때 10,000원 단위로 충전하는 로직
	 *
	 * @param money
	 * @param senderAccount
	 */
	private void autoCharge(long money, Account senderAccount) {

		long needMoney = money - senderAccount.getMoney();
		long chargeMoney = ((needMoney + CHARGE_AMOUNT - 1) / CHARGE_AMOUNT) * CHARGE_AMOUNT;

		if (!senderAccount.isChargeWithinDailyLimit(chargeMoney)) {
			throw new DailyChargeLimitExceededException();
		}
		senderAccount.deposit(chargeMoney);
	}

	private void validateTransactional(String senderAccountNumber, Transaction transaction) {
		if (!transaction.getSenderAccountNumber().equals(senderAccountNumber)) {
			throw new UnauthorizedTransactionException();
		}

		if (!transaction.getStatus().equals(PENDING_DEPOSIT)) {
			throw new InvalidTransactionStatusException();
		}
	}

	private void validateReceiverAccountNumber(String accountNumber) {
		accountQueryService.findAccount(accountNumber);
	}

}
