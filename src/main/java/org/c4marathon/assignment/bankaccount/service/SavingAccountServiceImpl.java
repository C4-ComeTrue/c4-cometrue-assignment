package org.c4marathon.assignment.bankaccount.service;

import java.util.List;

import org.c4marathon.assignment.bankaccount.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.product.ProductManager;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingAccountServiceImpl implements SavingAccountService {

	private final MemberRepository memberRepository;
	private final SavingAccountRepository savingAccountRepository;
	private final ProductManager productManager;

	// 논리적으로 member가 없을 수 없기에 쿼리 한 번만 날리는 getReferenceById를 사용하고 싶다.
	// 근데 만약 어떤 방식으로든 예외가 발생한다면 어떻게 해야하지? 뭐가 더 나은 방법인지 잘 모르겠다.
	// 성능 vs 안정성 차이 같은데 실무에선 어떻게 하는걸까?
	@Override
	public void create(long memberPk, String productName) {
		SavingAccount savingAccount = new SavingAccount();
		Integer rate = productManager.getRate(productName);
		if (rate == null) {
			throw AccountErrorCode.PRODUCT_NOT_FOUND.accountException("ProductManager에서 이율 조회 중 없는 상품의 이율 조회로 예외 발생.");
		}
		savingAccount.init(productName, rate);

		Member member = memberRepository.getReferenceById(memberPk);
		savingAccount.addMember(member);
		savingAccountRepository.save(savingAccount);
	}

	@Override
	public List<SavingAccountResponseDto> getSavingAccountInfo(long memberPk) {
		List<SavingAccount> savingAccount = savingAccountRepository.findSavingAccount(memberPk);

		return savingAccount.stream()
			.map(account -> SavingAccountResponseDto.builder()
				.accountPk(account.getAccountPk())
				.savingMoney(account.getSavingMoney())
				.rate(account.getRate())
				.productName(account.getProductName())
				.build())
			.toList();
	}
}
