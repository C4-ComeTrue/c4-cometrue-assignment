package org.c4marathon.assignment.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.Shipment;
import org.c4marathon.assignment.domain.ShipmentStatus;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ShipmentService {

	private final ShipmentRepository shipmentRepository;
	private final OrderService orderService;

	protected void issueTracker(Long orderId, String trackingNumber){
		Order order = orderService.findById(orderId);
		order.setRefundable(false);
		Shipment shipment = new Shipment();
		shipment.setOrder(order);
		shipment.setCourier("대한통운"); // 일단 통일합니다.
		shipment.setRegisterDate(LocalDateTime.now());
		shipment.setTrackingNumber(trackingNumber);
		Shipment save = shipmentRepository.save(shipment);
		order.setShipment(save);
		order.setShipmentStatus(ShipmentStatus.DISPATCHED);
	}

	protected void completion(Long orderId){
		Order order = orderService.findById(orderId);
		Optional<Shipment> optionalShipment = shipmentRepository.findByOrder(order);

		if(optionalShipment.isEmpty()){
			throw ErrorCd.NO_SUCH_ITEM.serviceException("배송 조회를 할 수 없음, 송장 번호를 다시 조회");
		}

		Shipment shipment = optionalShipment.get();
		shipment.setCompletedDate(LocalDateTime.now());

		order.setShipmentStatus(ShipmentStatus.COMPLETED);
	}
}
