package org.c4marathon.assignment.service;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.CartItemRepository;
import org.c4marathon.assignment.service.dto.CartItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService {

	private final ItemService itemService;

	private final CartItemRepository cartItemRepository;

	@Transactional
	public CartItem addCart(CartItemDTO cartItemDTO, Member customer){
		memberValidation(customer);

		Item item = itemService.findById(cartItemDTO.getItemId());

		CartItem cartItem = new CartItem();
		cartItem.setItem(item);
		cartItem.setCount(cartItemDTO.getCount());
		cartItem.setMember(customer);
		return cartItemRepository.save(cartItem);
	}

	@Transactional
	public void removeCart(Long cartItemId, Member customer){
		memberValidation(customer);

		CartItem cartItem = findCartItemById(cartItemId, customer);
		cartItemRepository.delete(cartItem);
	}

	@Transactional(readOnly = true)
	public CartItem findCartItemById(Long id, Member customer){
		Optional<CartItem> optionalCartItem = cartItemRepository.findCartItemByIdAndMember(id, customer);
		if(optionalCartItem.isEmpty()){
			throw ErrorCd.NO_SUCH_ITEM.serviceException("해당 제품이 장바구니에 없습니다.");
		}
		return optionalCartItem.get();
	}

	@Transactional(readOnly = true)
	public List<CartItem> getAllCartItem(Member customer){
		return cartItemRepository.findAllByCustomer(customer);
	}

	private void memberValidation(Member member){
		if(member.getMemberType() != MemberType.ROLE_CUSTOMER){
			throw ErrorCd.NO_PERMISSION.serviceException("소비자만 장바구니 기능을 사용할 수 있습니다");
		}
	}

}
