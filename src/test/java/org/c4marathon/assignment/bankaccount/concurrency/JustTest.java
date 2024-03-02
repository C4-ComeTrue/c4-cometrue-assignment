package org.c4marathon.assignment.bankaccount.concurrency;

import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;
import org.c4marathon.assignment.bankaccount.service.MainAccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JustTest {

	@Autowired
	MainAccountService mainAccountService;

	@Test
	void threadPoolTest() {
		MainAccountResponseDto mainAccountInfo = mainAccountService.getMainAccountInfo(4);
		mainAccountService.sendToOtherAccount(1, 4, 1000);
		// try {
		// 	Thread.sleep(2000);
		// } catch (Exception e) {
		// 	System.out.println(e);
		// }
		MainAccountResponseDto newMainAccountInfo = mainAccountService.getMainAccountInfo(4);
		Assertions.assertEquals(mainAccountInfo.money() + 1000 * 10, newMainAccountInfo.money());
		System.out.println("first : " + mainAccountInfo.money() + ", next: " + newMainAccountInfo.money());
	}
}
