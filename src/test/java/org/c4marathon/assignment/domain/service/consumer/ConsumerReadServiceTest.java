package org.c4marathon.assignment.domain.service.consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.service.ConsumerReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

public class ConsumerReadServiceTest extends ServiceTestSupport {

	@InjectMocks
	private ConsumerReadService consumerReadService;

	@DisplayName("이메일로 소비자 조회 시")
	@Nested
	class ExistsByEmail {

		@DisplayName("email에 해당하는 회원이 존재하면 true를 반환한다.")
		@Test
		void returnTrue_when_existsConsumer() {
			given(consumerRepository.existsByEmail(anyString())).willReturn(true);
			assertThat(consumerReadService.existsByEmail("email")).isTrue();
		}

		@DisplayName("email에 해당하는 회원이 존재하지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_notExistsConsumer() {
			given(consumerRepository.existsByEmail(anyString())).willReturn(false);
			assertThat(consumerReadService.existsByEmail("email")).isFalse();
		}
	}

	@DisplayName("id로 조회 시")
	@Nested
	class FindById {

		@DisplayName("조회할 데이터가 존재하면 Consumer를 반환한다.")
		@Test
		void should_returnConsumer_when_entityExist() {
			Consumer consumer = mock(Consumer.class);

			given(consumerRepository.findById(anyLong()))
				.willReturn(Optional.of(consumer));

			assertThat(consumerReadService.findById(1L))
				.isNotNull();
		}

		@DisplayName("조회할 데이터가 없다면 예외를 반환한다.")
		@Test
		void should_returnException_when_existsNotExist() {
			assertThatThrownBy(() -> consumerReadService.findById(1L))
				.isInstanceOf(BaseException.class)
				.hasMessage(ErrorCode.CONSUMER_NOT_FOUND_BY_ID.getMessage());
		}
	}
}
