package org.c4marathon.assignment.global.auth;

import org.c4marathon.assignment.domain.seller.entity.Seller;

public class SellerThreadLocal {

	private static final ThreadLocal<Seller> SELLER_THREAD_LOCAL;

	static {
		SELLER_THREAD_LOCAL = new ThreadLocal<>();
	}

	public static void set(Seller seller) {
		SELLER_THREAD_LOCAL.set(seller);
	}

	public static void remove() {
		SELLER_THREAD_LOCAL.remove();
	}

	public static Seller get() {
		return SELLER_THREAD_LOCAL.get();
	}
}
