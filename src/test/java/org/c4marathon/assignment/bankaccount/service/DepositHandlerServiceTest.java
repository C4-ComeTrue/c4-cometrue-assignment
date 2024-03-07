package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SendRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DepositHandlerServiceTest {

	@InjectMocks
	DepositHandlerService depositHandlerService;
	@Mock
	MainAccountRepository mainAccountRepository;
	@Mock
	SendRecordRepository sendRecordRepository;

	@Nested
	@DisplayName("완료되지 않은 이체 작업 처리 테스트")
	class DepositMoney {
		long accountPk = 1;
		long money = 1000;
		long recordPk = 1;

		@Test
		@DisplayName("이미 완료된 이체 작업은 재요청 되어도 입금 처리를 하지 않아야 한다.")
		void request_with_duplicated_deposit_record() {
			// Given
			given(sendRecordRepository.checkRecord(anyLong())).willReturn(0);

			// When
			depositHandlerService.doDeposit(accountPk, money, recordPk);

			// Then
			then(mainAccountRepository).should(times(0)).deposit(anyLong(), anyLong());
		}

		@Test
		@DisplayName("완료되지 않은 작업을 완료할 때 해당 로그의 변경 결과가 1인 경우만 메인 계좌에 입금을 실시한다.")
		void request_with_record_update_count_is_one() {
			// Given
			given(sendRecordRepository.checkRecord(anyLong())).willReturn(1);

			// When
			depositHandlerService.doDeposit(accountPk, money, recordPk);

			// Then
			then(mainAccountRepository).should(times(1)).deposit(anyLong(), anyLong());
		}
	}

}
