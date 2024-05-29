package org.c4marathon.assignment.domain.entity;

import static org.assertj.core.api.Assertions.*;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ConsumerTest {

	@DisplayName("Consumer Entity test")
	@Nested
	class ConsumerEntityTest {

		private Consumer consumer;

		@BeforeEach
		void setUp() {
			consumer = new Consumer("email", "address");
		}

		@DisplayName("consumer entity의 메서드 수행 시, 필드가 변경된다.")
		@Test
		void updateField_when_invokeEntityMethod() {
			consumer.addBalance(100L);
			assertThat(consumer.getBalance()).isEqualTo(100L);
			consumer.decreaseBalance(100L);
			assertThat(consumer.getBalance()).isZero();
			consumer.updatePoint(100L);
			assertThat(consumer.getPoint()).isEqualTo(100L);
		}
	}
}
