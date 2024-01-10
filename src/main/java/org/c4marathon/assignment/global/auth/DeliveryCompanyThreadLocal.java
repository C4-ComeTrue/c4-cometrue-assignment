package org.c4marathon.assignment.global.auth;

import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeliveryCompanyThreadLocal {

	private static final ThreadLocal<DeliveryCompany> DELIVERY_COMPANY_THREAD_LOCAL;

	static {
		DELIVERY_COMPANY_THREAD_LOCAL = new ThreadLocal<>();
	}

	public static void set(DeliveryCompany deliveryCompany) {
		DELIVERY_COMPANY_THREAD_LOCAL.set(deliveryCompany);
	}

	public static void remove() {
		DELIVERY_COMPANY_THREAD_LOCAL.remove();
	}

	public static DeliveryCompany get() {
		return DELIVERY_COMPANY_THREAD_LOCAL.get();
	}
}
