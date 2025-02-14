package org.c4marathon.assignment.transactional.service;

import java.util.List;

import org.c4marathon.assignment.global.event.transactional.TransactionalCreateEvent;
import org.c4marathon.assignment.transactional.domain.TransferTransactional;
import org.c4marathon.assignment.transactional.domain.TransactionalStatus;
import org.c4marathon.assignment.transactional.domain.repository.TransactionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionalService {
	private final TransactionalRepository transactionalRepository;

	@Transactional
	public void createTransactional(TransactionalCreateEvent request) {

		TransferTransactional transactional = TransferTransactional.create(
			request.senderAccountId(),
			request.receiverAccountId(),
			request.amount(),
			request.type(),
			request.status(),
			request.sendTime()
		);
		transactionalRepository.save(transactional);
	}

	@Transactional(readOnly = true)
	public List<TransferTransactional> findTransactionalByStatusWithLastId(TransactionalStatus status, Long lastId, int size) {
		if (lastId == null) {
			return transactionalRepository.findTransactionalByStatus(status, size);
		}
		return transactionalRepository.findTransactionalByStatusWithLastId(status, lastId, size);
	}

}
