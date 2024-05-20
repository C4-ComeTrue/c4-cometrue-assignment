package org.c4marathon.assignment.bankaccount.async.deposit;

import java.util.concurrent.TimeUnit;

import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;
import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.bankaccount.service.MainAccountService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class DepositHandlerServiceTest {

	@Autowired
	MainAccountService mainAccountService;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	MainAccountRepository mainAccountRepository;

	@Autowired
	SavingAccountRepository savingAccountRepository;

	@Autowired
	@Qualifier("depositExecutor")
	ThreadPoolTaskExecutor executor;

	private Member[] member;
	private MainAccount mainAccount;
	private SavingAccount savingAccount;
	private long[] mainAccountPk;
	private long[] savingAccountPk;

	@Nested
	@DisplayName("이체 실패 테스트")
	class SendToMainAccount {
		@BeforeEach
		void init() {
			createAccount();
		}

		@AfterEach
		void accountClear() {
			clearAccount();
		}

		long money = 1000;

		@Test
		@DisplayName("어떤 계좌에 입금이 실패한 경우, 롤백한다.")
		void if_deposit_fail_then_rollback() throws InterruptedException {
			// Given
			long wrongAccountPk = 5;
			MainAccountResponseDto originMainAccount = mainAccountService.getMainAccountInfo(mainAccountPk[0]);

			// When
			mainAccountService.sendToOtherAccount(mainAccountPk[0], wrongAccountPk, money);
			executor.getThreadPoolExecutor().awaitTermination(2, TimeUnit.SECONDS);

			// Then
			MainAccountResponseDto resultMainAccount = mainAccountService.getMainAccountInfo(mainAccountPk[0]);
			MainAccountResponseDto resultMainAccount1 = mainAccountService.getMainAccountInfo(mainAccountPk[1]);
			MainAccountResponseDto resultMainAccount2 = mainAccountService.getMainAccountInfo(mainAccountPk[2]);

			assertEquals(originMainAccount.money(), resultMainAccount.money());
		}
	}

	void createAccount() {
		mainAccountPk = new long[3];
		savingAccountPk = new long[3];
		member = new Member[3];
		for (int i = 0; i < 3; i++) {
			int money = 100000;
			mainAccount = new MainAccount();
			mainAccount.charge(money);
			mainAccountRepository.save(mainAccount);

			member[i] = Member.builder()
				.memberId("testId" + i)
				.password("testPass" + i)
				.memberName("testName" + i)
				.phoneNumber("testPhone" + i)
				.mainAccountPk(mainAccount.getAccountPk())
				.build();
			memberRepository.save(member[i]);

			savingAccount = new SavingAccount("free", 500);
			savingAccount.addMember(member[i]);
			savingAccountRepository.save(savingAccount);

			mainAccountPk[i] = mainAccount.getAccountPk();
			savingAccountPk[i] = savingAccount.getAccountPk();
		}
	}

	void clearAccount() {
		for (int i = 0; i < 3; i++) {
			savingAccount = savingAccountRepository.findById(savingAccountPk[i]).get();
			mainAccount = mainAccountRepository.findById(mainAccountPk[i]).get();
			savingAccountRepository.delete(savingAccount);
			mainAccountRepository.delete(mainAccount);
			memberRepository.delete(member[i]);
		}
	}

}
