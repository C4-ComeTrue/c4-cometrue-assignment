package org.c4marathon.assignment.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.dto.TransactionInfo;
import org.c4marathon.assignment.domain.type.TransactionState;
import org.c4marathon.assignment.global.QueryTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionScheduledService {
	private static final int BATCH_SIZE = 1000;
	private static final String TEMP_TABLE_NAME = "temp_transaction_sum";

	private final TransactionProcessor transactionProcessor;
	private final JdbcTemplate jdbcTemplate;
	private final PlatformTransactionManager transactionManager;

	/**
	 * 데드라인이 지난 송금은 자동 취소합니다. 커서 기반으로 배치 처리합니다.
	 * 임시 테이블에 (송금 계좌, 되돌릴 금액)을 저장하고 이를 토대로 계좌 내역을 한 번에 업데이트합니다.
	 */
	@Scheduled(cron = "${transaction.auto-cancel-interval}")
	public void cancelPendingAuto() {
		LocalDateTime end = LocalDateTime.now();

		QueryTemplate.selectAndExecuteWithCursorAndTx(transactionManager,
			info -> transactionProcessor.findAllAutoCancelInfo(info == null ? null : info.getId(), end, TransactionState.PENDING, BATCH_SIZE),
			updateAllInBatch(),
			BATCH_SIZE);
	}

	private Consumer<List<TransactionInfo>> updateAllInBatch() {
		return infos -> {
			Map<String, Long> accountBalanceMap = infos.stream()
				.collect(Collectors.groupingBy(TransactionInfo::getSenderAccountNumber,
					Collectors.summingLong(TransactionInfo::getBalance))
				);

			List<Long> transactionIds = infos.stream().map(TransactionInfo::getId).toList();

			createTempSavingTable();
			transactionProcessor.updateState(transactionIds, TransactionState.PENDING, TransactionState.CANCELLED);
			saveSumToTempTable(accountBalanceMap);
			batchCancel();
			dropTempSavingTable();
		};
	}

	private void createTempSavingTable() {
		jdbcTemplate.execute("""
			DROP TABLE IF EXISTS %s
		""".formatted(TEMP_TABLE_NAME));

		jdbcTemplate.execute("""
			CREATE TABLE %s(
			    sender_account_number VARCHAR(50),
			    balance BIGINT
			)
		""".formatted(TEMP_TABLE_NAME));
	}

	private void saveSumToTempTable(Map<String, Long> accountBalanceMap) {
		jdbcTemplate.batchUpdate("""
					INSERT INTO %s(sender_account_number, balance)
					VALUES (?, ?)
				""".formatted(TEMP_TABLE_NAME), accountBalanceMap.entrySet(), accountBalanceMap.size(),
			(ps, map) -> {
				ps.setString(1, map.getKey());
				ps.setLong(2, map.getValue());
			});
	}

	private void batchCancel() {
		jdbcTemplate.update("""
				UPDATE account a
					JOIN %s tts ON a.account_number = tts.sender_account_number
				SET a.balance = a.balance + tts.balance
			""".formatted(TEMP_TABLE_NAME));
	}

	private void dropTempSavingTable() {
		jdbcTemplate.execute("""
			DROP TABLE %s;
		""".formatted(TEMP_TABLE_NAME));
	}

}
