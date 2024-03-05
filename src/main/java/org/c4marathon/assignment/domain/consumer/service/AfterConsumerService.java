package org.c4marathon.assignment.domain.consumer.service;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.pointlog.repository.PointLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

/**
 * 환불 또는 구매 확정 이후에 발생되는 이벤트를 정의한 클래스
 * 환불 트랜잭션 커밋 이벤트 발생 시, 구매자가 사용한 금액과 포인트를 환불
 * 구매 확정 트랜잭션 커밋 이벤트 발생 시, 구매 포인트를 적립
 */
@Service
@RequiredArgsConstructor
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
