package org.c4marathon.assignment.domain.pay.service;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.pay.dto.request.ChargePayRequest;
import org.c4marathon.assignment.domain.pay.entity.Pay;
import org.c4marathon.assignment.domain.pay.repository.PayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PayService {

	private final PayRepository payRepository;
	private final ConsumerRepository consumerRepository;

	@Transactional
	public void chargePay(ChargePayRequest request, Consumer consumer) {
		savePay(request, consumer);
		consumer.addBalance(request.amount());
		consumerRepository.save(consumer);
	}

	private void savePay(ChargePayRequest request, Consumer consumer) {
		payRepository.save(Pay.builder()
			.consumer(consumer)
			.amount(request.amount())
			.build());
	}
}
