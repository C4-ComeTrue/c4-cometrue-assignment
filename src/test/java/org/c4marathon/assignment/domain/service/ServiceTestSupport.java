package org.c4marathon.assignment.domain.service;

import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.delivery.repository.DeliveryRepository;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.c4marathon.assignment.domain.order.repository.OrderRepository;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductRepository;
import org.c4marathon.assignment.domain.pay.repository.PayRepository;
import org.c4marathon.assignment.domain.product.repository.ProductRepository;
import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.c4marathon.assignment.global.auth.ConsumerThreadLocal;
import org.c4marathon.assignment.global.auth.DeliveryCompanyThreadLocal;
import org.c4marathon.assignment.global.auth.SellerThreadLocal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public abstract class ServiceTestSupport {

	@LocalServerPort
	public int port;
	@Autowired
	protected ConsumerRepository consumerRepository;
	@Autowired
	protected OrderProductRepository orderProductRepository;
	@Autowired
	protected OrderRepository orderRepository;
	@Autowired
	protected DeliveryRepository deliveryRepository;
	@Autowired
	protected DeliveryCompanyRepository deliveryCompanyRepository;
	@Autowired
	protected ProductRepository productRepository;
	@Autowired
	protected SellerRepository sellerRepository;
	@Autowired
	protected PayRepository payRepository;

	@AfterEach
	void tearDown() {
		orderProductRepository.deleteAllInBatch();
		productRepository.deleteAllInBatch();
		sellerRepository.deleteAllInBatch();
		orderRepository.deleteAllInBatch();
		deliveryRepository.deleteAllInBatch();
		deliveryCompanyRepository.deleteAllInBatch();
		payRepository.deleteAllInBatch();
		consumerRepository.deleteAllInBatch();
		ConsumerThreadLocal.remove();
		DeliveryCompanyThreadLocal.remove();
		SellerThreadLocal.remove();
	}
}
