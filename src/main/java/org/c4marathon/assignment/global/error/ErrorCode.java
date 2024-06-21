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
	NOT_POSSIBLE_CREATE_REVIEW(NOT_FOUND, "해당 product에 대한 구매 이력이 존재하지 않거나, 리뷰 작성 가능 기간이 지났습니다."),
	ALREADY_EVENT_EXISTS(CONFLICT, "해당 name에 대한 event가 이미 존재합니다."),
	ALREADY_DISCOUNT_POLICY_EXISTS(CONFLICT, "해당 name에 대한 discount_policy가 이미 존재합니다."),
	DISCOUNT_POLICY_NOT_FOUND(NOT_FOUND, "요청에 해당하는 discount_policy가 존재하지 않습니다."),
	EVENT_NOT_FOUND(NOT_FOUND, "요청에 해당하는 event가 존재하지 않습니다."),
	ALREADY_COUPON_EXISTS(CONFLICT, "해당 name에 대한 coupon이 이미 존재합니다."),
	COUPON_NOT_FOUND(NOT_FOUND, "요청에 해당하는 쿠폰 종류가 존재하지 않습니다."),
	RESOURCE_EXPIRED(GONE, "더 이상 사용할 수 없는 자원입니다."),
	COUPON_NOT_ISSUABLE(BAD_REQUEST, "더 이상 해당 쿠폰을 발급할 수 없습니다."),
	SINGLE_COUPON_AVAILABLE_PER_EVENT(CONFLICT, "하나의 이벤트에 하나의 쿠폰만 발급 가능합니다."),
	ISSUED_COUPON_NOT_FOUND(NOT_FOUND, "요청에 해당하는 발급된 쿠폰이 존재하지 않습니다."),
	INVALID_COUPON_EXPIRED_TIME(BAD_REQUEST, "쿠폰 유효기간은 이벤트 기간 이후일 수 없습니다."),
	ALREADY_USED_COUPON(BAD_REQUEST, "이미 사용된 쿠폰입니다."),
	COUPON_NOT_USABLE(BAD_REQUEST, "더 이상 해당 쿠폰을 사용할 수 없습니다."),
	EXCESSIVE_POINT_USE(BAD_REQUEST, "사용하려는 포인트가 주문 금액보다 많습니다."),

	SERVER_ERROR(INTERNAL_SERVER_ERROR, "request was interrupted. please try again.");

	private final HttpStatus status;
	private final String message;

	public BaseException baseException() {
		return new BaseException(this.name(), message);
	}

	public BaseException baseException(String debugMessage, Object... args) {
		return new BaseException(this.name(), message, String.format(debugMessage, args));
	}
}
