package org.c4marathon.assignment.domain.consumer.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.global.error.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumerService {

	private final ConsumerRepository consumerRepository;

	public void signup(SignUpRequest request) {
		if (request.getAddress() == null) {
			throw new BaseException(CONSUMER_NEED_ADDRESS);
		}
		if (consumerRepository.existsByEmail(request.getEmail())) {
			throw new BaseException(ALREADY_CONSUMER_EXISTS);
		}

		saveConsumer(request);
	}

	@Transactional
	public void saveConsumer(SignUpRequest request) {
		consumerRepository.save(Consumer.builder()
			.email(request.getEmail())
			.address(request.getAddress())
			.build());
	}
}
