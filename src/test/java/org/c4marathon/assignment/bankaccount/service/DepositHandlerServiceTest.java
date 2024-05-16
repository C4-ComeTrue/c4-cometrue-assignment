package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepositHandlerServiceTest {

	@InjectMocks
	DepositHandlerService depositHandlerService;
	@Mock
	MainAccountRepository mainAccountRepository;

	@Nested
	@DisplayName("완료되지 않은 이체 작업 처리 테스트")
	class DepositMoney {
		long sendPk = 2;
		long depositPk = 1;
		long money = 1000;
		long recordPk = 1;
		String streamId = "key";

	}

}
