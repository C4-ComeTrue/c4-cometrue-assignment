package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.exception.async.AccountAsyncErrorCode;
import org.c4marathon.assignment.bankaccount.exception.async.AccountAsyncException;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SendRollbackHandlerServiceTest {

	@InjectMocks
	SendRollbackHandlerService sendRollbackHandlerService;

	@Mock
	MainAccountRepository mainAccountRepository;

	@Nested
	@DisplayName("입금시 발생한 예외 처리 테스트")
	class SendRollback {

		@Test
		@DisplayName("존재하지 않는 계좌라면 SEND_ROLLBACK_FAILED 예외가 발생한다")
		void exception_with_non_exist_account() {
			// Given
			long sendPk = 1;
			long depositPk = 2;
			long money = 3;
			int result = 0;
			given(mainAccountRepository.deposit(anyLong(), anyLong())).willReturn(result);

			// when
			AccountAsyncException accountAsyncException = assertThrows(AccountAsyncException.class, () -> {
				sendRollbackHandlerService.rollBackDeposit(sendPk, depositPk, money);
			});

			// Then
			assertEquals(AccountAsyncErrorCode.SEND_ROLLBACK_FAILED.name(), accountAsyncException.getErrorName());
		}
	}
}
