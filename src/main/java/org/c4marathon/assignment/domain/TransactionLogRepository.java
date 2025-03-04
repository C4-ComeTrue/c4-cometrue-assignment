package org.c4marathon.assignment.domain;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TransactionLogRepository extends MongoRepository<TransactionLog, String> {
	@Query(sort = "{ 'sendTime': -1, 'transactionId': -1 }")
	Optional<TransactionLog> findFirstBy();
}
