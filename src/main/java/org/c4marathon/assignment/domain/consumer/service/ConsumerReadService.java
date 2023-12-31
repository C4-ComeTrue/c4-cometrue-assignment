package org.c4marathon.assignment.domain.consumer.service;

import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ConsumerReadService {

	private final ConsumerRepository consumerRepository;

	public Boolean existsByEmail(String email) {
		return consumerRepository.existsByEmail(email);
	}
}
