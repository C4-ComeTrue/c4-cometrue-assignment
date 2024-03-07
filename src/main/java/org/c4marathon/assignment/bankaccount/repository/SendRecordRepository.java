package org.c4marathon.assignment.bankaccount.repository;

import java.util.List;

import org.c4marathon.assignment.bankaccount.entity.SendRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SendRecordRepository extends JpaRepository<SendRecord, Long> {
	@Modifying
	@Query("""
		update SendRecord sr
		set sr.completion = true
		where sr.recordPk = :recordPk and sr.completion = false
		""")
	int checkRecord(@Param("recordPk") long recordPk);

	@Query("select sa from SendRecord sa where sa.completion = false")
	List<SendRecord> findNonCompletedDeposit();
}
