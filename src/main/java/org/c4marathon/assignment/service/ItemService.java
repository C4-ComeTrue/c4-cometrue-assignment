package org.c4marathon.assignment.service;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {
	private final MemberService memberService;
	private final ItemRepository itemRepository;

	// 판매자는 판매할 상품을 등록할 수 있다.
	@Transactional
	public Item saveItem(Item item, Long memberId) {
		Member member = memberService.findCustomerId(memberId);
		if (!member.getMemberType().equals(MemberType.ROLE_SELLER)) {
			throw ErrorCd.NO_PERMISSION.serviceException("잘못된 접근입니다",
				"판매자가 아닌 고객이 상품 등록 접근 시도 : 아이디 : ", member.getUserId());
		}
		item.setSeller(member);
		item.setDisplayed(false); // 처음 제품을 저장하는 경우, 제품이 바로 표시되지 않음.
		return itemRepository.save(item);
	}

	// 판매자는 등록한 상품을 수정할 수 있다.
	@Transactional
	public Item updateItem(Item item, Long itemId, Long memberId) {
		Member member = memberService.findCustomerId(memberId);
		if (!item.getSeller().equals(member)) { // 수정 전, 사용자 검증
			throw ErrorCd.NO_PERMISSION.serviceException("잘못된 접근입니다",
				"판매자가 아닌 다른 계정이 상품 수정을 시도함 : 아이디 : ", member.getUserId());
		}

		Optional<Item> updateTarget = itemRepository.findById(itemId);
		if (updateTarget.isEmpty()) { // 상품 존재 여부 검증
			throw ErrorCd.NO_SUCH_ITEM.serviceException("상품이 존재하지 않습니다",
				"itemId:", itemId, "에 해당하는 상품이 없음 : 아이디 : ", member.getUserId());
		}

		Item target = updateTarget.get();
		target.setDescription(item.getDescription());
		target.setStock(item.getStock());
		target.setPrice(item.getPrice());
		target.setDisplayed(item.isDisplayed());

		return target;
	}

	@Transactional(readOnly = true)
	// 제품의 기본키를 이용하여 특정 제품을 조회한다.
	public Item findById(Long id) {
		Optional<Item> item = itemRepository.findById(id);
		if (item.isEmpty()) {
			throw ErrorCd.NO_SUCH_ITEM.serviceException("상품을 찾을 수 없습니다",
				"상품을 찾을 수 없습니다 : 아이디 : ", id.toString());
		}
		return item.get();
	}

	@Transactional(readOnly = true)
	// 특정 판매자를 통해 해당 판매자가 판매하고 있는 모든 상품을 조회한다.
	public List<Item> findBySeller(Member seller) {
		if (!seller.getMemberType().equals(MemberType.ROLE_SELLER)) {
			throw ErrorCd.INVALID_ARGUMENT.serviceException("조회할 수 없습니다",
				"고객 회원이 판매목록 조회를 시도 : 아이디 : ", seller.getUserId());
		}
		return itemRepository.findAllByMemberId(seller.getMemberPk());
	}

}
