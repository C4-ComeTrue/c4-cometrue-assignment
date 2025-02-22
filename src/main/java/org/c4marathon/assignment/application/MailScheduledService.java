package org.c4marathon.assignment.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.TransactionRepository;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.c4marathon.assignment.domain.dto.TransactionRemindInfo;
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
	private static final int REMIND_HOURS = 24;
	private static final int BATCH_SIZE = 1000;

	private final JavaMailSender mailSender;
	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	private static ReminderCursor cursor =  new ReminderCursor(0L, LocalDateTime.MIN);

	@Scheduled(cron = "${transaction.remind-interval}")
	public void remindPendingTransaction() {
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusHours(REMIND_HOURS);
		cursor = new ReminderCursor(cursor.id, cursor.deadline.isBefore(start) ? start : cursor.deadline);

		ReminderThreadPoolExecutor threadPoolExecutor = new ReminderThreadPoolExecutor();
		threadPoolExecutor.init();

		QueryTemplate.selectAndExecuteWithCursor(getRemindInfos(end), sendReminderAll(threadPoolExecutor), BATCH_SIZE);

	}

	private Function<TransactionRemindInfo, List<TransactionRemindInfo>> getRemindInfos(LocalDateTime end) {
		return info -> {
			List<TransactionRemindInfo> allRemindInfoByCursor = transactionRepository.findAllRemindInfoByCursor(
				cursor.id, cursor.deadline, end, BATCH_SIZE);

			if (!allRemindInfoByCursor.isEmpty()) {
				TransactionRemindInfo lastInfo = allRemindInfoByCursor.get(allRemindInfoByCursor.size() - 1);
				cursor.update(lastInfo.getId(), lastInfo.getDeadline());
			}

			return allRemindInfoByCursor;
		};
	}

	private Consumer<List<TransactionRemindInfo>> sendReminderAll(ReminderThreadPoolExecutor threadPoolExecutor) {
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

	private void sendReminder(String receiverEmail, LocalDateTime deadline, long money) {
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
		mimeMessage.setFrom("rksidksrksi@naver.com");

		return mimeMessage;
	}

	private static class ReminderCursor {
		long id;
		LocalDateTime deadline;

		ReminderCursor(long id, LocalDateTime deadline) {
			this.id = id;
			this.deadline = deadline;
		}

		void update(long id, LocalDateTime deadline) {
			this.id = id;
			this.deadline = deadline;
		}
	}
}
