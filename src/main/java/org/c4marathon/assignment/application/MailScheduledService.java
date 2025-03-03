package org.c4marathon.assignment.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.TransactionRepository;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.c4marathon.assignment.domain.dto.TransactionInfo;
import org.c4marathon.assignment.domain.type.TransactionState;
import org.c4marathon.assignment.global.QueryTemplate;
import org.c4marathon.assignment.global.ReminderThreadPoolExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailScheduledService {
	private static final int REMIND_LIMIT_HOURS = 24;
	private static final int BATCH_SIZE = 1000;
	private static final int REMIND_INTERVAL_MINUTES = 5;

	private final JavaMailSender mailSender;
	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	/**
	 * 실행 시간부터 5분 전 시간까지 PENDING 거래 내역들 중 자동 취소까지
	 * 24시간 남은 거래 내역을 모두 찾고 메일 전송.
	 */
	@Scheduled(cron = "${transaction.scheduled.interval.remind}")
	public void remindPendingTransaction() {
		Instant end = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(REMIND_LIMIT_HOURS, ChronoUnit.HOURS);
		Instant initStart = end.minus(REMIND_INTERVAL_MINUTES, ChronoUnit.MINUTES);

		ReminderThreadPoolExecutor threadPoolExecutor = new ReminderThreadPoolExecutor();
		threadPoolExecutor.init();

		QueryTemplate.selectAndExecuteWithCursor(
			transactionInfo ->
				transactionRepository.findAllInfoBy(
					transactionInfo == null ? 0 : transactionInfo.getId(),
					transactionInfo == null ? initStart : transactionInfo.getDeadline(),
					end,
					TransactionState.PENDING.name(),
					BATCH_SIZE),
			sendReminderAll(threadPoolExecutor),
			BATCH_SIZE);

		threadPoolExecutor.shutdown();
	}

	private Consumer<List<TransactionInfo>> sendReminderAll(ReminderThreadPoolExecutor threadPoolExecutor) {
		return transactionRemindInfos ->
			transactionRemindInfos.forEach(info ->
				threadPoolExecutor.execute(() -> {
					Account receiverAccount = accountRepository.findByAccountNumber(info.getReceiverAccountNumber())
						.orElseThrow(() -> new RuntimeException("찾을 수 없는 계좌입니다."));
					User receiver = userRepository.findById(receiverAccount.getUserId())
						.orElseThrow(() -> new RuntimeException("찾을 수 없는 사용자입니다."));

					sendReminder(receiver.getEmail(), info.getDeadline(), info.getBalance());
				})
			);
	}

	private void sendReminder(String receiverEmail, Instant deadline, long money) {
		try {
			MimeMessage message = getMessage(receiverEmail);
			message.setSubject("찾아가지 않은 금액이 있어요");
			message.setText("""
				%d원을 아직 찾아가지 않았어요! 아래 기간 내에 빠르게 찾아가세요!
				취소 기간: %s
				""".formatted(money, deadline.toString())
			);

			mailSender.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException("메일 작성 중 에러", e); // TODO: 메일 에러로 전환
		}
	}

	private MimeMessage getMessage(String receiver) throws MessagingException {
		MimeMessage mimeMessage = mailSender.createMimeMessage();

		mimeMessage.addRecipients(Message.RecipientType.TO, receiver);
		mimeMessage.setFrom("ZZAMBA");

		return mimeMessage;
	}
}
