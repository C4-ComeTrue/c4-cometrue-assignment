package org.c4marathon.assignment.domain.service;

import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductEntry;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.repository.DeliveryRepository;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.repository.OrderRepository;
import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductRepository;
import org.c4marathon.assignment.domain.pay.repository.PayRepository;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.repository.ProductRepository;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class ServiceTestSupport {

	@Mock
	protected ConsumerRepository consumerRepository;
	@Mock
	protected OrderProductRepository orderProductRepository;
	@Mock
	protected OrderRepository orderRepository;
	@Mock
	protected DeliveryRepository deliveryRepository;
	@Mock
	protected DeliveryCompanyRepository deliveryCompanyRepository;
	@Mock
	protected ProductRepository productRepository;
	@Mock
	protected SellerRepository sellerRepository;
	@Mock
	protected PayRepository payRepository;

	@Mock
	protected Order order;
	@Mock
	protected Delivery delivery;
	@Mock
	protected Consumer consumer;
	@Mock
	protected OrderProduct orderProduct;
	@Mock
	protected PurchaseProductEntry purchaseProductEntry;
	@Mock
	protected DeliveryCompany deliveryCompany;
	@Mock
	protected Product product;
	@Mock
	protected Seller seller;
}
