package org.c4marathon.assignment.service;

import java.util.Random;
import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.domain.SavingAccount;
import org.c4marathon.assignment.dto.request.SavingAccountRequestDto;
import org.c4marathon.assignment.repository.SavingAccountRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingAccountService {
	private final SavingAccountRepository savingAccountRepository;
	private final MainAccountService mainAccountService;
	static int counter = 50;

	@Transactional
	public void createSavingAccount(SavingAccountRequestDto requestDto) {
		MainAccount mainAccount = mainAccountService.getMainAccount(requestDto.mainAccountId());
		SavingAccount savingAccount = new SavingAccount(mainAccount, createAccountNumber(), requestDto.balance(), requestDto.rate());
		savingAccountRepository.save(savingAccount);
	}

	/**
	 * 계좌 번호 생성
	 * */
	private String createAccountNumber(){
		Random random = new Random();
		int createNum = 0;
		String ranNum = "";
		String randomNum = "";

		for (int i=0; i<7; i++) {
			createNum = random.nextInt(9);
			ranNum = Integer.toString(createNum);
			randomNum += ranNum;
		}
		String bankNum = "3333";
		String countAccountNum = String.format("%02d",counter);

		counter++;
		String accountNum = bankNum+"-"+countAccountNum+"-"+randomNum;
		return accountNum;
	}
}
