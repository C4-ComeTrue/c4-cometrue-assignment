package org.c4marathon.assignment.bankaccount.service;

import java.util.List;

import org.c4marathon.assignment.bankaccount.entity.SendRecord;
import org.c4marathon.assignment.bankaccount.repository.SendRecordRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulerService {
	private final SendRecordRepository sendRecordRepository;
	private final DepositHandlerService depositHandlerService;

	// 메소드 실행 후 1분 주기로 반복
	@Scheduled(fixedDelay = 60000)
	public void sendRecordSchedule() {
		List<SendRecord> nonCompletedDeposit = sendRecordRepository.findNonCompletedDeposit();
		nonCompletedDeposit.stream()
			.forEach(sendRecord -> depositHandlerService.doDeposit(sendRecord.getDepositPk(), sendRecord.getMoney(),
				sendRecord.getRecordPk()));
	}
}
