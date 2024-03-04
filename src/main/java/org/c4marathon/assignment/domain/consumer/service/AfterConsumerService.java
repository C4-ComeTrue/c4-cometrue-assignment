package org.c4marathon.assignment.domain.consumer.service;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.pointlog.repository.PointLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AfterConsumerService {

	private final ConsumerRepository consumerRepository;
	private final ConsumerReadService consumerReadService;
	private final PointLogRepository pointLogRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void afterConfirm(PointLog pointLog) {
		Consumer consumer = consumerReadService.findById(pointLog.getConsumerId());
		consumer.updatePoint(pointLog.getPoint());
		consumerRepository.save(consumer);
		pointLogRepository.deleteById(pointLog.getId());
	}
}
