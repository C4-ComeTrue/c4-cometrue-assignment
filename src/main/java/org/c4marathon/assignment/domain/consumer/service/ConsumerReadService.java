package org.c4marathon.assignment.domain.consumer.service;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumerReadService {

	private final ConsumerRepository consumerRepository;

	/**
	 * email로 Consumer 존재 여부 확인
	 */
	@Transactional(readOnly = true)
	public Boolean existsByEmail(String email) {
		return consumerRepository.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public Consumer findById(Long id) {
		return consumerRepository.findById(id)
			.orElseThrow(() -> ErrorCode.CONSUMER_NOT_FOUND_BY_ID.baseException("id: %s", id));
	}
}
