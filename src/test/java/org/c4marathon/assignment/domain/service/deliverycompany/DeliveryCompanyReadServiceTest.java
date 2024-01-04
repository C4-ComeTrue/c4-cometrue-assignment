package org.c4marathon.assignment.domain.service.deliverycompany;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.repository.DeliveryRepository;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DeliveryCompanyReadServiceTest extends ServiceTestSupport {

	@Autowired
	private DeliveryCompanyReadService deliveryCompanyReadService;
	@Autowired
	private DeliveryCompanyRepository deliveryCompanyRepository;
	@Autowired
	private DeliveryRepository deliveryRepository;

	@DisplayName("최소 배송 정보를 가진 배송 회사 조회 시")
	@Nested
	class FindMinimumCountOfDelivery {

		@AfterEach
		void tearDown() {
			deliveryRepository.deleteAllInBatch();
			deliveryCompanyRepository.deleteAllInBatch();
		}

		@DisplayName("배송 정보가 1, 2개인 배송 회사가 존재하면, 배송 정보가 1개인 배송 회사가 조회된다.")
		@Test
		void selectOneDeliveryInfo_when_oneAndTwoInfoExists() {
			DeliveryCompany company1 = deliveryCompanyRepository.save(DeliveryCompany.builder()
				.email("email")
				.build());
			DeliveryCompany company2 = deliveryCompanyRepository.save(DeliveryCompany.builder()
				.email("email2")
				.build());

			deliveryRepository.saveAll(List.of(
				Delivery.builder()
					.deliveryCompany(company1)
					.address("ad")
					.invoiceNumber("num")
					.build(),
				Delivery.builder()
					.deliveryCompany(company1)
					.address("ad")
					.invoiceNumber("num")
					.build(),
				Delivery.builder()
					.deliveryCompany(company2)
					.address("ad")
					.invoiceNumber("num")
					.build()
			));

			DeliveryCompany minimumCountOfDelivery = deliveryCompanyReadService.findMinimumCountOfDelivery();
			assertThat(minimumCountOfDelivery.getId()).isEqualTo(company2.getId());
		}
	}
}
