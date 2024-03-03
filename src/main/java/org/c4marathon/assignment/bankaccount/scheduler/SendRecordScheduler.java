package org.c4marathon.assignment.bankaccount.scheduler;

import java.util.List;

import org.c4marathon.assignment.bankaccount.entity.SendRecord;
import org.c4marathon.assignment.bankaccount.repository.SendRecordRepository;
import org.c4marathon.assignment.bankaccount.service.DepositHandlerService;
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
	 *
	 * 만약 큐에서 처리하는 속도가 느려서 큐에 있는 작업을 또 넣어주는 일이 발생하면 어떻게 해야할지 고민입니다.
	 * 자주 발생하지 않을 이체 실패니까 넉넉하게 시간을 잡아서 완료되지 않은 이체 작업을 처리할지,
	 * 인덱스를 하나 추가해서 [completion = false]인 조건을 추가하고 지금처럼 자주 확인할지 고민입니다.
	 */
	@Scheduled(fixedDelay = 60000)
	private void sendRecordSchedule() {
		List<SendRecord> nonCompletedDeposit = sendRecordRepository.findNonCompletedDeposit();
		nonCompletedDeposit.stream()
			.forEach(sendRecord -> depositHandlerService.doDeposit(sendRecord.getDepositPk(), sendRecord.getMoney(),
				sendRecord.getRecordPk()));
	}
}
