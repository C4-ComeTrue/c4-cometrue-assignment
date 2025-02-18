package org.c4marathon.assignment.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.c4marathon.assignment.dto.DelayedTransferTransactionEvent;
import org.c4marathon.assignment.dto.ImmediateTransferTransactionEvent;
import org.c4marathon.assignment.dto.TransferTransactionEvent;
import org.c4marathon.assignment.dto.request.PostMainAccountReq;
import org.c4marathon.assignment.dto.request.PostSavingsAccountReq;
import org.c4marathon.assignment.dto.request.TransferReq;
import org.c4marathon.assignment.dto.request.WithdrawMainAccountReq;
import org.c4marathon.assignment.dto.response.MainAccountInfoRes;
import org.c4marathon.assignment.dto.response.TransferRes;
import org.c4marathon.assignment.dto.response.WithdrawInfoRes;
import org.c4marathon.assignment.entity.Account;
import org.c4marathon.assignment.entity.SavingsAccount;
import org.c4marathon.assignment.entity.TransactionType;
import org.c4marathon.assignment.entity.User;
import org.c4marathon.assignment.event.TransferTransactionEventPublisher;
import org.c4marathon.assignment.exception.CustomException;
import org.c4marathon.assignment.exception.ErrorCode;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.SavingsAccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
	private static final long CHARGE_UNIT = 10_000L;
	private final SavingsAccountRepository savingsAccountRepository;
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final TransferTransactionEventPublisher transferTransactionEventPublisher;

	/**
	 * 적금 계좌를 생성한다.
	 */
	public void createSavingsAccount(PostSavingsAccountReq postSavingsAccountReq) {
		User user = userRepository.findByEmail(postSavingsAccountReq.email())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

		savingsAccountRepository.save(new SavingsAccount(user.getId()));
	}

	/**
	 * 메인 계좌에 돈 충전 기능
	 */
	@Transactional
	public MainAccountInfoRes depositMainAccount(PostMainAccountReq postMainAccountReq) {
		User user = getUserById(postMainAccountReq.userId());

		Account account = getAccountByIdWithWriteLock(user.getId());

		charge(postMainAccountReq.amount(), account);

		return new MainAccountInfoRes(account);
	}

	/**
	 * 메인 계좌에서 적금 계좌로 돈을 인출하는 기능
	 */
	@Transactional
	public WithdrawInfoRes withdrawForSavings(WithdrawMainAccountReq withdrawMainAccountReq) {
		User user = getUserById(withdrawMainAccountReq.userId());

		Account account = getAccountByIdWithWriteLock(user.getId());

		if (account.isBalanceInsufficient(withdrawMainAccountReq.amount())) {
			throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
		}

		SavingsAccount savingsAccount = savingsAccountRepository.findByIdAndUserIdWithWriteLock(
				withdrawMainAccountReq.savingsAccount(),
				withdrawMainAccountReq.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_SAVINGS_ACCOUNT));

		account.withdraw(withdrawMainAccountReq.amount());
		savingsAccount.deposit(withdrawMainAccountReq.amount());

		return new WithdrawInfoRes(account.getBalance(), savingsAccount.getBalance());
	}

	/**
	 * 송금 기능
	 * 1. 유효성 검증 수행
	 * 2. 이체자의 계좌 잔액 감소 및 송금 내역 추가
	 * 3. 송금 수취인의 계좌 잔액 증가 관련 이벤트 발행
	 */
	@Transactional
	public TransferRes transfer(TransferReq transferReq) {
		User sender = getUserById(transferReq.senderId());

		validateTransfer(transferReq, sender);

		Account receiverAccount = getAccountById(transferReq.receiverMainAccount());

		Account senderAccount = getAccountByIdWithWriteLock(sender.getId());

		if (senderAccount.isBalanceInsufficient(transferReq.amount())) {
			long chargeAmount = ((transferReq.amount() - senderAccount.getBalance()) / CHARGE_UNIT + 1) * CHARGE_UNIT;

			charge(chargeAmount, senderAccount);
		}

		senderAccount.withdraw(transferReq.amount());

		publishTransferTransactionEvent(transferReq, sender, senderAccount, receiverAccount.getUserId());

		return new TransferRes(senderAccount.getBalance());
	}

	private void publishTransferTransactionEvent(TransferReq transferReq, User sender, Account senderAccount, Long receiverId) {
		TransferTransactionEvent event;
		if(transferReq.type() == TransactionType.IMMEDIATE) {
			event = ImmediateTransferTransactionEvent.builder()
				.userName(sender.getUsername())
				.senderMainAccount(senderAccount.getId())
				.receiverMainAccount(transferReq.receiverMainAccount())
				.amount(transferReq.amount())
				.receiverId(receiverId)
				.build();
		} else {
			event = DelayedTransferTransactionEvent.builder()
				.userName(sender.getUsername())
				.senderMainAccount(senderAccount.getId())
				.receiverMainAccount(transferReq.receiverMainAccount())
				.amount(transferReq.amount())
				.receiverId(receiverId)
				.build();
		}
		transferTransactionEventPublisher.publishTransferTransactionEvent(event);
	}

	private void validateTransfer(TransferReq transferReq, User sender) {
		if (sender.getMainAccountId() == transferReq.receiverMainAccount()) {
			throw new CustomException(ErrorCode.INVALID_TRANSFER_REQUEST);
		}
	}

	/**
	 * 0시 0분 0초에 일일 한도를 초기화 하는 스케줄러
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void initDailyCharge() {
		log.debug("{} init daily charge", Thread.currentThread().getName());
		accountRepository.initDailyChargedAmount(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	private void charge(long amount, Account account) {
		if (account.isDailyLimitExceeded(amount)) {
			throw new CustomException(ErrorCode.EXCEEDED_DEPOSIT_LIMIT);
		}
		account.deposit(amount);
	}

	private User getUserById(long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ID));
		return user;
	}

	private Account getAccountByIdWithWriteLock(long mainAccount) {
		Account account = accountRepository.findByIdWithWriteLock(mainAccount)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_MAIN_ACCOUNT));
		return account;
	}

	private Account getAccountById(long receiverMainAccount) {
		Account account = accountRepository.findById(receiverMainAccount)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_MAIN_ACCOUNT));
		return account;
	}
}
