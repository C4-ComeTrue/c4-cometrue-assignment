package org.c4marathon.assignment.domain.consumer.service;

import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
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
}
