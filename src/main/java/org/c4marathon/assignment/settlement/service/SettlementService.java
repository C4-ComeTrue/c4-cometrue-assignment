package org.c4marathon.assignment.settlement.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.settlement.document.MemberInfoDocument;
import org.c4marathon.assignment.settlement.document.SettlementInfoDocument;
import org.c4marathon.assignment.settlement.dto.request.DivideMoneyRequestDto;
import org.c4marathon.assignment.settlement.dto.response.SettlementInfoResponseDto;
import org.c4marathon.assignment.settlement.exception.SettlementErrorCode;
import org.c4marathon.assignment.settlement.util.RandomUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {

	private final MongoTemplate mongoTemplate;

	/**
	 * 1/n 정산은 전체 금액을 사람 수로 나눈 몫을 기본으로 이체할 금액으로 한다.
	 * 이후 정산할 사람들의 목록을 랜덤으로 셔플하여 앞에서부터 나머지 한도 내에서 1원씩 이체할 금액을 추가한다.
	 *
	 * 랜덤 정산은 나름의 최소한의 한도를 정하고 남은 금액을 랜덤으로 나눠 가지도록 했다.
	 */
	public void divideMoney(long requestAccountPk, String requestMemberName, DivideMoneyRequestDto requestDto) {
		int totalNumber = requestDto.totalNumber();
		if (totalNumber != requestDto.memberInfoList().size()) {
			throw SettlementErrorCode.WRONG_PARAMETER.settlementException(
				"totalNumber = " + totalNumber + ", list size = " + requestDto.memberInfoList().size());
		}

		// 랜덤으로 나눠주기 위해 셔플
		Collections.shuffle(requestDto.memberInfoList());

		List<MemberInfoDocument> memberInfoList;
		if (requestDto.isRandom()) {
			memberInfoList = getRandomSettlementInfo(requestDto, totalNumber);
		} else {
			memberInfoList = getEquallySettlementInfo(requestDto, totalNumber);
		}

		SettlementInfoDocument settleInfo = new SettlementInfoDocument(requestAccountPk, requestMemberName, totalNumber,
			requestDto.totalMoney(),
			memberInfoList);
		System.out.println(settleInfo);

		try {
			mongoTemplate.insert(settleInfo);
		} catch (DataAccessException exception) {
			throw CommonErrorCode.INTERNAL_SERVER_ERROR.commonException("정산 데이터 저장 실패");
		}

	}

	/**
	 *
	 * 자신의 계좌가 포함된 정산 요청 리스트를 반환한다.
	 */
	public List<SettlementInfoResponseDto> getSettlementInfoList(long accountPk) {
		try {
			Query query = new Query(Criteria.where("memberInfoList.accountPk").is(accountPk));
			return mongoTemplate.find(query, SettlementInfoDocument.class)
				.stream()
				.map(settlementInfoDocument -> new SettlementInfoResponseDto(settlementInfoDocument))
				.collect(Collectors.toList());
		} catch (DataAccessException exception) {
			throw CommonErrorCode.INTERNAL_SERVER_ERROR.commonException("정산 데이터 조회 실패");
		}
	}

	private List<MemberInfoDocument> getEquallySettlementInfo(DivideMoneyRequestDto requestDto, int totalNumber) {
		long initMoney = requestDto.totalMoney() / totalNumber; // 1인당 보내야 하는 최소 금액
		long leftMoney = requestDto.totalMoney() % totalNumber; // 최소 금액을 뺀 나머지 금액

		// 랜덤으로 정렬하여 앞에서부터 leftMoney 한도 내에서 1원씩 받도록 한다.
		List<MemberInfoDocument> memberInfoList = requestDto.memberInfoList()
			.stream()
			.map(member -> new MemberInfoDocument(member.accountPk(), member.memberName(), initMoney))
			.collect(
				Collectors.toList());
		Collections.shuffle(memberInfoList);

		for (int i = 0; i < totalNumber && leftMoney-- > 0; i++) {
			memberInfoList.get(i).plusLeftMoney();
		}

		return memberInfoList;
	}

	private List<MemberInfoDocument> getRandomSettlementInfo(DivideMoneyRequestDto requestDto, int totalNumber) {
		long totalMoney = requestDto.totalMoney();
		long initMoney = totalMoney / (totalNumber * 2);

		List<MemberInfoDocument> memberInfoList = requestDto.memberInfoList()
			.stream()
			.map(member -> new MemberInfoDocument(member.accountPk(), member.memberName(), initMoney))
			.collect(
				Collectors.toList());
		Collections.shuffle(memberInfoList);
		totalMoney -= (initMoney * totalNumber);

		long randomMoney;
		for (int i = 0; i < totalNumber - 1; i++) {
			randomMoney = RandomUtils.getRandomMoney(totalMoney + 1);
			memberInfoList.get(i).plusRandomMoney(randomMoney);
			totalMoney -= randomMoney;

		}
		memberInfoList.get(totalNumber - 1).plusRandomMoney(totalMoney);

		return memberInfoList;

	}
}
