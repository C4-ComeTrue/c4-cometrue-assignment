package org.c4marathon.assignment.settlement.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.settlement.document.MemberInfoDocument;
import org.c4marathon.assignment.settlement.document.SettlementInfoDocument;
import org.c4marathon.assignment.settlement.dto.request.DivideMoneyRequestDto;
import org.c4marathon.assignment.settlement.exception.SettlementErrorCode;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {

	private final MongoTemplate mongoTemplate;
	private final MongoTransactionManager mongoTransactionManager;

	/**
	 * 1/n 정산은 전체 금액을 사람 수로 나눈 몫을 기본으로 이체할 금액으로 한다.
	 * 이후 정산할 사람들의 목록을 랜덤으로 셔플하여 앞에서부터 나머지 한도 내에서 1원씩 이체할 금액을 추가한다.
	 *
	 * @param requestAccountPk : 정산을 요청한 사용자 계좌 pk
	 * @param requestDto : 정산을 해야하는 사용자들 계좌 pk
	 */
	public void divideEqually(long requestAccountPk, String requestMemberName, DivideMoneyRequestDto requestDto) {
		int totalNumber = requestDto.totalNumber();
		if (totalNumber != requestDto.memberInfoList().size()) {
			throw SettlementErrorCode.WRONG_PARAMETER.settlementException(
				"totalNumber = " + totalNumber + ", list size = " + requestDto.memberInfoList().size());
		}
		long minMoney = requestDto.totalMoney() / totalNumber; // 1인당 보내야 하는 최소 금액
		long leftMoney = requestDto.totalMoney() % totalNumber; // 최소 금액을 뺀 나머지 금액
		List<Long> personalSendMoney = new ArrayList<>(Collections.nCopies(totalNumber, minMoney));

		// 랜덤으로 정렬하여 앞에서부터 leftMoney 한도 내에서 1원씩 받도록 한다.
		List<MemberInfoDocument> memberInfoList = requestDto.memberInfoList()
			.stream()
			.map(member -> new MemberInfoDocument(member.accountPk(), member.memberName(), minMoney))
			.collect(
				Collectors.toList());
		Collections.shuffle(memberInfoList);
		for (int i = 0; i < totalNumber && leftMoney-- > 0; i++) {
			memberInfoList.get(i).plusLeftMoney();
		}

		SettlementInfoDocument settleInfo = new SettlementInfoDocument(requestAccountPk, requestMemberName, totalNumber,
			requestDto.totalMoney(),
			memberInfoList);

		try {
			mongoTemplate.insert(settleInfo);
		} catch (DataAccessException exception) {
			throw CommonErrorCode.INTERNAL_SERVER_ERROR.commonException("정산 데이터 저장 실패");
		} finally {
			// TODO 서버 -> 클라이언트 메세지 보내기..?
		}

	}
}
