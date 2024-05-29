package org.c4marathon.assignment.global.error;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

	BIND_ERROR(BAD_REQUEST, "입력값 중 검증에 실패한 값이 존재합니다."),
	CONSUMER_NEED_ADDRESS(BAD_REQUEST, "소비자인 경우 회원가입 시 주소가 필요합니다."),
	ALREADY_CONSUMER_EXISTS(CONFLICT, "이미 이메일에 해당하는 소비자가 존재합니다."),
	ALREADY_PRODUCT_NAME_EXISTS(CONFLICT, "이미 존재하는 상품명입니다."),
	PRODUCT_NOT_FOUND(NOT_FOUND, "요청에 해당하는 상품이 존재하지 않습니다."),
	NOT_ENOUGH_BALANCE(BAD_REQUEST, "구매자의 충전된 캐시가 부족합니다."),
	ORDER_NOT_FOUND(NOT_FOUND, "요청에 해당하는 주문이 존재하지 않습니다."),
	NO_PERMISSION(FORBIDDEN, "해당 자원에 대한 접근 권한이 존재하지 않습니다."),
	REFUND_NOT_AVAILABLE(BAD_REQUEST, "환불이 불가능한 상태입니다."),
	ALREADY_SELLER_EXISTS(CONFLICT, "이미 이메일에 해당하는 판매자가 존재합니다."),
	ALREADY_DELIVERY_COMPANY_EXISTS(CONFLICT, "이미 이메일에 해당하는 배송 회사가 존재합니다."),
	DELIVERY_NOT_FOUND(NOT_FOUND, "요청에 해당하는 배송 정보가 존재하지 않습니다."),
	INVALID_DELIVERY_STATUS_REQUEST(BAD_REQUEST, "해당 배송 상태로는 변경할 수 없습니다."),
	DELIVERY_COMPANY_NOT_FOUND(NOT_FOUND, "요청에 해당하는 배송 회사가 존재하지 않습니다."),
	DELIVERY_STATUS_NOT_FOUND(NOT_FOUND, "요청에 해당하는 배송 상태가 존재하지 않습니다."),
	CONFIRM_NOT_AVAILABLE(BAD_REQUEST, "구매 확정이 불가능한 상태입니다."),
	NOT_ENOUGH_PRODUCT_STOCK(BAD_REQUEST, "상품의 재고가 부족합니다."),
	NOT_ENOUGH_POINT(BAD_REQUEST, "사용할 포인트가 부족합니다."),
	CONSUMER_NOT_FOUND_BY_ID(NOT_FOUND, "id에 해당하는 Consumer가 존재하지 않습니다."),
	REVIEW_ALREADY_EXISTS(CONFLICT, "해당 product에 대한 review가 이미 존재합니다."),
	NOT_POSSIBLE_CREATE_REVIEW(NOT_FOUND, "해당 product에 대한 구매 이력이 존재하지 않거나, 리뷰 작성 가능 기간이 지났습니다.");

	private final HttpStatus status;
	private final String message;

	public BaseException baseException() {
		return new BaseException(this.name(), message);
	}

	public BaseException baseException(String debugMessage, Object... args) {
		return new BaseException(this.name(), message, String.format(debugMessage, args));
	}
}
