package org.c4marathon.assignment.settlement.service;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.common.exception.CommonException;
import org.c4marathon.assignment.settlement.document.MemberInfoDocument;
import org.c4marathon.assignment.settlement.document.SettlementInfoDocument;
import org.c4marathon.assignment.settlement.dto.request.DivideMoneyRequestDto;
import org.c4marathon.assignment.settlement.dto.request.MemberInfo;
import org.c4marathon.assignment.settlement.dto.response.SettlementInfoResponseDto;
import org.c4marathon.assignment.settlement.exception.SettlementErrorCode;
import org.c4marathon.assignment.settlement.exception.SettlementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

	@InjectMocks
	SettlementService settlementService;

	@Mock
	MongoTemplate mongoTemplate;

	long accountPk;
	String memberName;
	int totalNumber;
	long totalMoney;
	List<MemberInfo> memberInfoList;

	@BeforeEach
	void initSession() {
		accountPk = 1L;
		memberName = "user1";
		totalNumber = 3;
		totalMoney = 100000;
		memberInfoList = new ArrayList<>(
			List.of(new MemberInfo(2, "user2"), new MemberInfo(3, "user3"), new MemberInfo(4, "user4")));
	}

	@Nested
	@DisplayName("정산 요청 테스트")
	class SettleMoney {

		@Test
		@DisplayName("정산 요청 사용자의 수와 사용자 정보의 수가 일치하지 않으면 정산 요청이 실패한다.")
		void request_with_member_number_is_not_equal_totalNumber() {
			// given
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(1, totalMoney, true, memberInfoList);

			// When
			SettlementException settlementException = assertThrows(SettlementException.class, () -> {
				settlementService.divideMoney(accountPk, memberName, requestDto);
			});

			// Then
			assertEquals(SettlementErrorCode.WRONG_PARAMETER.name(), settlementException.getErrorName());
		}

		@Test
		@DisplayName("올바른 입력값으로 요청하면 정상적으로 정산 요청이 수행된다.")
		void request_with_valid_parameters() {
			// Given
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(totalNumber, totalMoney, false,
				memberInfoList);

			// When
			settlementService.divideMoney(accountPk, memberName, requestDto);

			// Then
			then(mongoTemplate).should(times(1)).insert(any(SettlementInfoDocument.class));
		}

		@Test
		@DisplayName("데이터베이스 오류로 정산 요청이 실패하면 INTERNAL_SERVER_ERROR를 반환한다.")
		void request_when_database_server_error() {
			// Given
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(totalNumber, totalMoney, false,
				memberInfoList);
			given(mongoTemplate.insert(any(SettlementInfoDocument.class))).willThrow(new DataAccessException("error") {
			});

			// When
			CommonException commonException = assertThrows(CommonException.class,
				() -> settlementService.divideMoney(accountPk, memberName, requestDto));

			// Then
			assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR.name(), commonException.getErrorName());
		}
	}

	@Nested
	@DisplayName("정산 정보 조회 테스트")
	class GetSettlementInfo {

		@Test
		@DisplayName("정산 정보 조회 요청 성공 테스트")
		void success_when_get_settlement_info_list() {
			// Given
			List<SettlementInfoDocument> responseDto = new ArrayList<>(
				List.of(new SettlementInfoDocument(2, "user2", 3, 10000,
					List.of(new MemberInfoDocument(1, "user1", 33334), new MemberInfoDocument(3, "user3", 33333),
						new MemberInfoDocument(4, "user4", 33333)))));

			Query query = new Query(Criteria.where("memberInfoList.accountPk").is(accountPk));
			when(mongoTemplate.find(query, SettlementInfoDocument.class)).thenReturn(responseDto);

			// When
			List<SettlementInfoResponseDto> settlementInfoList = settlementService.getSettlementInfoList(accountPk);

			// Then
			assertEquals(responseDto.size(), settlementInfoList.size());
			long sum = 0;
			for (MemberInfoDocument memberInfoDocument : responseDto.get(0).getMemberInfoList()) {
				sum += memberInfoDocument.getSettleMoney();
			}
			assertEquals(totalMoney, sum);
		}

		@Test
		@DisplayName("데이터베이스 오류로 정산 내역 조회가 실패하면 INTERNAL_SERVER_ERROR를 반환한다.")
		void request_when_database_server_error() {
			// Given
			Query query = new Query(Criteria.where("memberInfoList.accountPk").is(accountPk));
			given(mongoTemplate.find(query, SettlementInfoDocument.class)).willThrow(
				new DataAccessException("error") {
				});

			// When
			CommonException commonException = assertThrows(CommonException.class,
				() -> settlementService.getSettlementInfoList(accountPk));

			// Then
			assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR.name(), commonException.getErrorName());
		}
	}
}
