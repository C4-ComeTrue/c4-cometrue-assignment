package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingProduct;
import org.c4marathon.assignment.account.domain.SavingProductType;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingProductRepository;
import org.c4marathon.assignment.account.dto.SavingAccountCreateRequest;
import org.c4marathon.assignment.account.dto.SavingAccountCreateResponse;
import org.c4marathon.assignment.account.service.query.SavingAccountQueryService;
import org.c4marathon.assignment.global.util.AccountNumberUtil;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class SavingAccountServiceTest extends IntegrationTestSupport {
    @Autowired
    private SavingAccountQueryService savingAccountQueryService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SavingProductRepository savingProductRepository;

    @Autowired
    private SavingAccountService savingAccountService;

    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        savingAccountRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("적금 계좌를 생성한다.")
    @Test
    void createSavingAccount() {
        // given
        Member member = Member.create("test@test.com", "테스트", "testPassword");
        String mainAccountNumber = AccountNumberUtil.generateAccountNumber("3333");

        SavingProduct product = SavingProduct.create(3.0, SavingProductType.FREE, 24);
        savingProductRepository.save(product);

        Account mainAccount = Account.create(mainAccountNumber, 10000L);
        accountRepository.save(mainAccount);

        SavingAccountCreateRequest request = new SavingAccountCreateRequest(SavingProductType.FREE, product.getId(), 1000L);

        memberRepository.save(member);

        // when
        SavingAccountCreateResponse savingAccount = savingAccountService.createSavingAccount(mainAccountNumber, request);

        // then
        assertThat(savingAccount.savingAccountId()).isNotNull();
    }
}