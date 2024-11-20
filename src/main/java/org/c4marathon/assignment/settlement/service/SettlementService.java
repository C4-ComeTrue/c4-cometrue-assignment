package org.c4marathon.assignment.settlement.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.settlement.document.MemberInfoDocument;
import org.c4marathon.assignment.settlement.document.SettlementInfoDocument;
import org.c4marathon.assignment.settlement.dto.request.DivideMoneyRequestDto;
import org.c4marathon.assignment.settlement.dto.request.MemberInfo;
import org.c4marathon.assignment.settlement.dto.response.SettlementInfoResponseDto;
import org.c4marathon.assignment.settlement.exception.SettlementErrorCode;
import org.c4marathon.assignment.settlement.util.SettlementUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
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

		List<MemberInfoDocument> memberInfoList;
		if (requestDto.isRandom()) {
			memberInfoList = getRandomSettlementInfo(requestDto.memberInfoList(), requestDto.totalMoney(), totalNumber);
		} else {
			memberInfoList = getEquallySettlementInfo(requestDto, totalNumber);
		}

		SettlementInfoDocument settleInfo = new SettlementInfoDocument(requestAccountPk, requestMemberName, totalNumber,
			requestDto.totalMoney(), memberInfoList, LocalDateTime.now());

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
	public List<SettlementInfoResponseDto> getSettlementInfoList(long accountPk, String objectId) {
		try {
			ObjectId id = getId(objectId);

			Query query = new Query(Criteria.where("memberInfoList.accountPk").is(accountPk))
				.addCriteria(Criteria.where("_id").gt(id))
				.with(Sort.by(Sort.Order.desc("createdAt")))
				.limit(SettlementUtils.PAGE_SIZE);

			return mongoTemplate.find(query, SettlementInfoDocument.class)
				.stream()
				.map(settlementInfoDocument -> new SettlementInfoResponseDto(settlementInfoDocument))
				.toList();
		} catch (DataAccessException exception) {
			throw CommonErrorCode.INTERNAL_SERVER_ERROR.commonException("정산 데이터 조회 실패");
		}
	}

	private ObjectId getId(String objectId) {
		if (objectId == null || objectId.isEmpty()) {
			return new ObjectId(0, 0);
		}

		return new ObjectId(objectId);
	}

	/**
	 * 1/N 정산 메소드. 1원 단위까지 분배합니다.
	 * @param requestDto
	 * @param totalNumber
	 * @return
	 */
	private List<MemberInfoDocument> getEquallySettlementInfo(DivideMoneyRequestDto requestDto, int totalNumber) {
		long initMoney = requestDto.totalMoney() / totalNumber; // 1인당 보내야 하는 최소 금액
		long leftMoney = requestDto.totalMoney() % totalNumber; // 최소 금액을 뺀 나머지 금액

		List<MemberInfoDocument> memberInfoList = requestDto.memberInfoList()
			.stream()
			.map(member -> new MemberInfoDocument(member.accountPk(), member.memberName(), initMoney))
			.collect(Collectors.toList());

		// 랜덤으로 정렬하여 앞에서부터 leftMoney 한도 내에서 1원씩 받도록 한다.
		Collections.shuffle(memberInfoList);
		memberInfoList.stream()
			.forEach(member -> member.plusLeftMoney());

		return memberInfoList;
	}

	/**
	 * 랜덤 정산 메소드
	 * @param memberInfoList
	 * @param totalMoney
	 * @param totalNumber
	 * @return
	 */
	private List<MemberInfoDocument> getRandomSettlementInfo(List<MemberInfo> memberInfoList, long totalMoney,
		int totalNumber) {

		List<MemberInfoDocument> memberInfoDocumentList = memberInfoList.stream()
			.map(member -> new MemberInfoDocument(member.accountPk(), member.memberName(), 0))
			.toList();

		long range = totalMoney / SettlementUtils.MIN_UNIT + 1; // 최소 금액 단위로 나눌 때 나눠줘야 할 범위
		// 0번째 사람을 제외한 나머지 사람들에 대해 랜덤하게 분할
		for (int i = 1; i < totalNumber; i++) {
			long randomCount = SettlementUtils.getRandomMoney(range);
			range -= randomCount;
			long plusMoney = randomCount * SettlementUtils.MIN_UNIT;
			totalMoney -= plusMoney;
			memberInfoDocumentList.get(i).plusRandomMoney(plusMoney);
		}

		// 잔여 금액 할당
		memberInfoDocumentList.get(0).plusRandomMoney(totalMoney);

		return memberInfoDocumentList;

	}
}
