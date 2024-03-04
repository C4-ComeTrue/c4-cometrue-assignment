package org.c4marathon.assignment.domain.consumer.service;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.pointlog.repository.PointLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AfterConsumerService {

	private final ConsumerReadService consumerReadService;
	private final PointLogRepository pointLogRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(condition = "#pointLog.IsConfirm == true")
	public void afterConfirm(PointLog pointLog) {
		Consumer consumer = consumerReadService.findById(pointLog.getConsumerId());
		consumer.updatePoint(pointLog.getEarnedPoint());
		pointLogRepository.deleteById(pointLog.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(condition = "#pointLog.IsConfirm == false")
	public void afterRefund(PointLog pointLog) {
		Consumer consumer = consumerReadService.findById(pointLog.getConsumerId());
		consumer.addBalance(pointLog.getTotalAmount() - pointLog.getUsedPoint());
		consumer.updatePoint(pointLog.getUsedPoint());
	}
}
