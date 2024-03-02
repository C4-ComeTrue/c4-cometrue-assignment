package org.c4marathon.assignment.common.utils;

public final class ConstValue {
	public final class LimitConst {
		public static final long CHARGE_LIMIT = 3000000;
		public static final long CHARGE_AMOUNT = 10000;

		private LimitConst() {
		}
	}

	public final class ProductConst {
		public static final int DIVIDE_VALUE = 10000; // 이율 계산할 때 나누는 수

		private ProductConst() {
		}
	}
}
