package org.c4marathon.assignment.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

import static org.c4marathon.assignment.global.CommonUtils.Pair;

@Service
@RequiredArgsConstructor
public class MailScheduledService {
	private static final int REMIND_HOURS = 24;
	private static final int BATCH_SIZE = 1000;

	private final JavaMailSender mailSender;
	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	private static Pair<Long, LocalDateTime> cursor = Pair.of(0L, LocalDateTime.MIN); // {id, lastDeadline}

	/**
	 * 현재 시간부터 24시간 이후 시간 내 PENDING 상태인 거래 내역을 확인하고 메일로 보냅니다. 스레드 풀을 이용합니다.
	 * 배치를 통해 수행하며 cursor를 통해 마지막으로 메일을 보낸 거래를 추적합니다. 이를 통해 동일 거래에 대해 중복 메일이 전송되지 않습니다.
	 * 현재 cursor는 변수로 관리 중이지만 사실 캐시에 저장하는 데이터로, redis에 저장할 수도 있을 것 같습니다.
	 * 현재는 메일을 제대로 전송하지 못해도 그냥 넘어갑니다. 이는 메일이 그렇게 크게 중요하다고 생각이 들지 않기 때문입니다.
	 */
	@Scheduled(cron = "${transaction.remind-interval}")
	public void remindPendingTransaction() {
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusHours(REMIND_HOURS);
		cursor = Pair.of(cursor.getT(), cursor.getR().isBefore(start) ? start : cursor.getR());

		ReminderThreadPoolExecutor threadPoolExecutor = new ReminderThreadPoolExecutor();
		threadPoolExecutor.init();

		QueryTemplate.selectAndExecuteWithCursor(getRemindInfos(end), sendReminderAll(threadPoolExecutor), BATCH_SIZE);

		threadPoolExecutor.shutdown();
	}

	/**
	 * BATCH_SIZE만큼 메일을 보낼 PENDING 거래 내역을 가져옵니다.
	 * @param end
	 * @return
	 */
	private Supplier<List<TransactionInfo>> getRemindInfos(LocalDateTime end) {
		return () -> {
			List<TransactionInfo> remindInfos = transactionRepository.findAllInfoBy(
				cursor.getT(), cursor.getR(), end, TransactionState.PENDING.name(), BATCH_SIZE);

			if (!remindInfos.isEmpty()) {
				TransactionInfo lastInfo = remindInfos.get(remindInfos.size() - 1);
				cursor.update(lastInfo.getId(), lastInfo.getDeadline());
			}

			return remindInfos;
		};
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
		mimeMessage.setFrom("ZZAMBA");

		return mimeMessage;
	}
}
