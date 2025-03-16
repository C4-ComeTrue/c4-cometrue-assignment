package org.c4marathon.assignment.transaction.service.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.c4marathon.assignment.global.util.StringUtil;
import org.c4marathon.assignment.transaction.service.TransactionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionalScheduler {
	private final TransactionService transactionService;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

	//알맞은 시간 설정해야함
	@Scheduled(fixedRate = 10000)
	public void cancelExpiredTransactions() {
		transactionService.processCancelExpiredTransactions();
	}

	/**
	 * 매월 28일 오전 3시에 파티션을 추가
	 */
	@Scheduled(cron = "0 0 3 28 * *")
	public void createNextMonthPartition() {
		LocalDate nextMonth = LocalDate.now().plusMonths(1);
		String partitionName = "p" + nextMonth.format(formatter);
		String lessThanValue = String.valueOf(nextMonth.plusMonths(1).getYear() * 100 + nextMonth.plusMonths(1).getMonthValue());

		String addPartitionQuery = StringUtil.format(
			"ALTER TABLE transaction ADD PARTITION (PARTITION {} VALUES LESS THAN ({}))",
			partitionName, lessThanValue
		);

		try {
			jdbcTemplate.execute(addPartitionQuery);
			log.info("Partition '{}' successfully created.", partitionName);
		} catch (Exception e) {
			log.warn("Partition '{}' already exists or creation failed: {}", partitionName, e.getMessage());
		}
	}


	/**
	 * 매월 1일 오전 3시에 1년이 지난 파티션 삭제
	 * 2025-05-01일이면 2024-04월 송금 내역 파티션 테이블을 drop 한다.
	 */
	@Scheduled(cron = "0 0 3 1 * *")
	public void dropOldTransactionPartition() {
		// 1년 전의 파티션을 계산
		YearMonth oneYearAgo = YearMonth.now().minusYears(1).minusMonths(1);
		String partitionName = "p" + oneYearAgo.format(formatter);

		// 파티션 삭제 SQL
		String sql = StringUtil.format("ALTER TABLE transaction DROP PARTITION {}", partitionName);

		try {
			jdbcTemplate.execute(sql);
			log.info("Partition '{}' successfully dropped.", partitionName);
		} catch (Exception e) {
			log.warn("Failed to drop partition '{}'. It may not exist. Reason: {}", partitionName, e.getMessage());
		}
	}

/*

	@Scheduled(fixedRate = 10000)
	public void remindNotificationTransactions() {
		transactionService.processRemindNotifications();
	}
*/

}
