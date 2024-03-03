package org.c4marathon.assignment.bankaccount.service;

import java.util.List;

import org.c4marathon.assignment.bankaccount.entity.SendRecord;
import org.c4marathon.assignment.bankaccount.repository.SendRecordRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SendRecordScheduler {
	private final SendRecordRepository sendRecordRepository;
	private final DepositHandlerService depositHandlerService;

	/**
	 *
	 * 완전히 처리되지 않은 이체 작업을 이체 작업 큐에 넣어주는 스케줄
	 * 현재는 1분 주기로 실행
	 */
	@Scheduled(fixedDelay = 60000)
	private void sendRecordSchedule() {
		List<SendRecord> nonCompletedDeposit = sendRecordRepository.findNonCompletedDeposit();
		nonCompletedDeposit.stream()
			.forEach(sendRecord -> depositHandlerService.doDeposit(sendRecord.getDepositPk(), sendRecord.getMoney(),
				sendRecord.getRecordPk()));
	}
}
