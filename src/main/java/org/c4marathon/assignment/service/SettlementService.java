package org.c4marathon.assignment.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.domain.Settlement;
import org.c4marathon.assignment.domain.SettlementMember;
import org.c4marathon.assignment.domain.Transaction;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.enums.TransactionStatus;
import org.c4marathon.assignment.domain.enums.SettlementType;
import org.c4marathon.assignment.domain.enums.TransactionType;
import org.c4marathon.assignment.dto.request.ReceiveSettlementRequestDto;
import org.c4marathon.assignment.dto.request.RemittanceRequestDto;
import org.c4marathon.assignment.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.repository.SettlementRepository;
import org.c4marathon.assignment.repository.TransactionRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

	private final SettlementRepository settlementRepository;
	private final TransactionRepository transactionRepository;
	private final UserService userService;
	private final MainAccountService mainAccountService;
	private final JavaMailSender javaMailSender;
	private static final int MAX_RETRY_COUNT = 3;

	/**
	 * [Step3] 정산
	 * @param requestDto - requestAccountId, totalAmount, type
	 * */
	@Transactional
	public void divideMoney(SettlementRequestDto requestDto) {

		if (requestDto.type() == SettlementType.EQUAL) {
			divideEqual(requestDto);
		} else {
			divideRandom(requestDto);
		}
	}

	/**
	 * [Step3] 1/n정산
	 * 딱 나누어 떨어지지 않을 경우 정해진 순서 없이 아무나에게 1씩 추가 정산 요청
	 * */
	private void divideEqual(SettlementRequestDto requestDto) {
		List<Long> settlementMemberIds = requestDto.settlementMemberIds();
		int totalPeople = settlementMemberIds.size();
		int totalAmount = requestDto.totalAmount();

		long baseAmount = totalAmount / totalPeople;
		long remainAmount = totalAmount % totalPeople;

		Settlement settlement = new Settlement(requestDto.requestAccountId(), totalAmount, totalPeople,
			SettlementType.EQUAL, TransactionStatus.REQUESTED, null);

		//기본 금액 전체 할당
		List<SettlementMember> updatedMembers = settlementMemberIds.stream().map(memberId -> new SettlementMember(
			memberId, baseAmount, settlement
		)).collect(Collectors.toList());

		//남은 금액 1원씩 할당
		if (remainAmount > 0) {
			Collections.shuffle(updatedMembers);
			for (int i = 0; i < remainAmount; i++) {
				SettlementMember member = updatedMembers.get(i);
				member.updateAmount(1);
			}
		}

		settlement.addSettlementMembers(updatedMembers);
		settlementRepository.save(settlement);
	}

	/**
	 * [Step3] Random 정산 (0원 가능)
	 * 카카오페이의 돈 뿌리기 처럼 돈을 못받는? 정산하지 않아도 되는 사람이 생길 수도 있다.
	 * */
	private void divideRandom(SettlementRequestDto requestDto) {
		List<Long> settlementMemberIds = requestDto.settlementMemberIds();
		int totalPeople = settlementMemberIds.size();
		int totalAmount = requestDto.totalAmount();
		SecureRandom random = new SecureRandom();

		//Settlement
		Settlement settlement = new Settlement(requestDto.requestAccountId(), totalAmount, totalPeople,
			SettlementType.RANDOM, TransactionStatus.REQUESTED, null);

		//랜덤으로 돈 배정
		Collections.shuffle(settlementMemberIds);
		int remainAmount = totalAmount;
		List<SettlementMember> updatedMembers = new ArrayList<>();

		for (int i = 0; i < totalPeople - 1; i++) {
			int randomAmount = random.nextInt(remainAmount + 1);
			remainAmount -= randomAmount;
			if (randomAmount > 0) { // 0원을 받는 사람은 저장X
				updatedMembers.add(new SettlementMember(
					settlementMemberIds.get(i),
					randomAmount,
					settlement
				));
			}
		}

		//남은 돈이 있으면 마지막 사람에게 할당
		if (remainAmount > 0) {
			updatedMembers.add(new SettlementMember(
				settlementMemberIds.get(totalPeople - 1),
				remainAmount,
				settlement
			));
		}

		settlement.addSettlementMembers(updatedMembers);
		settlementRepository.save(settlement);
	}

	/**
	 * [Step4] 정산을 위한 송금 요청하기(대기)
	 *
	 * */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void requestRemittanceMoney(RemittanceRequestDto requestDto) {
		Transaction transaction = new Transaction(requestDto.requestAccountId(), requestDto.settlementMemberAccountId(),
			requestDto.amount(), TransactionStatus.REQUESTED, requestDto.type());
		transactionRepository.save(transaction);

		if (requestDto.type() == TransactionType.REALTIME) {
			recieveMoney(transaction); // ✅  돈바로 받는 로직이랑 같을텐데 일단 내 계좌에서 돈을 빼는 로직은 구현  + 돈 보내는 로직 다 구현
		} else {
			transaction.pendingTransaction(); // 상태 변환
			transactionRepository.save(transaction);
			sendNotification(requestDto.requestAccountId());
			//✅ 돈바로 받는 로직이랑 같을텐데 일단 내 계좌에서 돈을 빼는 로직은 구현
		}

	}


	/**
	 * [Step4] 돈을 받는 함수
	 * 전체가 다 정산되면 Settlement에 status Success로 변경
	 * */
	@Transactional
	public void receiveMoney(ReceiveSettlementRequestDto requestDto) {
		//requestDto.transactionId, requestDto.settlementMemberId
		Transaction transaction = transactionRepository.findByIdWithXLock(transactionId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TRANSACTION));

		if (transaction.getStatus() != TransactionStatus.PENDING) {
			throw new IllegalStateException("이미 완료된 송금입니다.");
		}

		MainAccount receiver = mainAccountRepository.findByIdWithXLock(transaction.getReceiverId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ACCOUNT));

		receiver.updateBalance(transaction.getAmount());
		transaction.completeTransaction();

		mainAccountRepository.save(receiver);
		transactionRepository.save(transaction);
	}

	/**
	 * [Step4] 돈을 보냈다는 알림 보내는 함수
	 * "송금 요청이 도착했습니다. 72시간 내에 수령해주세요!"
	 * */
	private void sendNotification(long requestAccountId) {
		final String MAIL_SUBJECT = "송금 요청이 도착했습니다. 72시간 내에 수령해주세요!";
		int attempt = 0;
		boolean isSent = false;

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MainAccount mainAccount = mainAccountService.getMainAccount(requestAccountId);
		User user = userService.getUser(mainAccount.getUser().getId());

		while (attempt < MAX_RETRY_COUNT && !isSent) {
			try {
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
				mimeMessageHelper.setTo(user.getEmail());
				mimeMessageHelper.setSubject(MAIL_SUBJECT);
				javaMailSender.send(mimeMessage);
				isSent = true;
			} catch (Exception e) {
				attempt++;
				log.warn("메일 전송 실패 ({}회차): {}, 에러: {}", attempt, user.getEmail(), e.getMessage());

				if (attempt < MAX_RETRY_COUNT) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						log.error("메일 전송 재시도 대기 중 인터럽트 발생: {}", ie.getMessage());
					}
				} else {
					log.error("메일 전송 최종 실패: {}", user.getEmail());
				}
			}
		}
	}

	/**
	 * [Step4] 24시간 남은 송금 리마인드 알림
	 * */
	@Scheduled(fixedRate = 60000) // 1분마다 실행 ==> 굳이 10분 마다 해도될듯?
	public void sendRemindersForPendingTransactions() {
		List<Transaction> pendingTransactions = transactionRepository.findRemindableTransactions(LocalDateTime.now().minusHours(48));

		for (Transaction transaction : pendingTransactions) {
			sendNotification(transaction.getReceiverId(), "송금을 받을 시간이 24시간 남았습니다!");
		}
	}

	/**
	 * [Step4] (72시간 초과 시 자동 취소)
	 * */
	@Scheduled(fixedRate = 60000) // 1분마다 실행
	@Transactional
	public void cancelExpiredTransactions() {
		List<Transaction> expiredTransactions = transactionRepository.findExpiredTransactions(LocalDateTime.now().minusHours(72));

		for (Transaction transaction : expiredTransactions) {
			transaction.cancelTransaction();
			transactionRepository.save(transaction);
			sendNotification(transaction.getSenderId(), "72시간이 지나 송금이 자동 취소되었습니다.");
		}
	}


}
