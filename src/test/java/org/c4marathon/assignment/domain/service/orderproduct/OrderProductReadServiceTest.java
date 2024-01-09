package org.c4marathon.assignment.domain.service.orderproduct;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.constant.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderProductReadServiceTest extends ServiceTestSupport {

	@Autowired
	private OrderProductReadService orderProductReadService;
	private List<Order> orders;

	@BeforeEach
	void setUp() {
		Consumer consumer = consumerRepository.save(createconsumer());
		DeliveryCompany deliveryCompany = deliveryCompanyRepository.save(createDeliveryCompany());
		List<Delivery> deliveries = deliveryRepository.saveAll(List.of(
			createDelivery(deliveryCompany),
			createDelivery(deliveryCompany)));
		orders = orderRepository.saveAll(List.of(
			createOrder(consumer, deliveries.get(0)),
			createOrder(consumer, deliveries.get(1))));
		Seller seller = sellerRepository.save(createSeller());
		Product product = productRepository.save(createProduct(seller));
		orderProductRepository.saveAll(List.of(
			createOrderProduct(orders.get(0), product),
			createOrderProduct(orders.get(0), product),
			createOrderProduct(orders.get(1), product)));
	}

	@DisplayName("id로 조회 시")
	@Nested
	class FindByOrderJoinFetchProduct {

		@DisplayName("orderId에 해당하는 OrderProduct가 모두 조회된다.")
		@Test
		void selectOrderProductsRelatedOrderId_when_findByOrderJoinFetchProduct() {
			List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetchProduct(
				orders.get(0).getId());
			assertThat(orderProducts).hasSize(2);
			assertThat(orderProducts.get(0).getProduct()).isNotNull();
		}
	}

	@DisplayName("id로 조회 시2")
	@Nested
	class FindByOrderJoinFetchProductAndSeller {

		@DisplayName("orderId에 해당하는 OrderProduct가 모두 조회된다.")
		@Test
		void selectOrderProductsRelatedOrderId_when_findByOrderJoinFetchProductAndSeller() {
			List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetchProductAndSeller(
				orders.get(0).getId());
			assertThat(orderProducts).hasSize(2);
			assertThat(orderProducts.get(0).getProduct()).isNotNull();
			assertThat(orderProducts.get(0).getProduct().getSeller()).isNotNull();
		}
	}

	private Product createProduct(Seller seller) {
		return Product.builder()
			.name("name")
			.description("desciption")
			.stock(100)
			.amount(100L)
			.seller(seller)
			.build();
	}

	private Seller createSeller() {
		return Seller.builder()
			.email("email")
			.build();
	}

	private Delivery createDelivery(DeliveryCompany deliveryCompany) {
		return Delivery.builder()
			.address("add")
			.invoiceNumber("invoice")
			.deliveryCompany(deliveryCompany)
			.build();
	}

	private DeliveryCompany createDeliveryCompany() {
		return DeliveryCompany.builder()
			.email("email")
			.build();
	}

	private Consumer createconsumer() {
		return Consumer.builder()
			.email("email")
			.address("add")
			.build();
	}

	private Order createOrder(Consumer consumer, Delivery delivery) {
		return Order.builder()
			.orderStatus(OrderStatus.COMPLETE_PAYMENT)
			.delivery(delivery)
			.consumer(consumer)
			.build();
	}

	private OrderProduct createOrderProduct(Order order, Product product) {
		return OrderProduct.builder()
			.order(order)
			.amount(100L)
			.quantity(100)
			.product(product)
			.build();
	}
}
