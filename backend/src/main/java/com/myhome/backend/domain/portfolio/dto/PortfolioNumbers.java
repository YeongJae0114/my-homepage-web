package com.myhome.backend.domain.portfolio.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Response rounding only; calculations keep full precision. */
public final class PortfolioNumbers {

	private PortfolioNumbers() {
	}

	public static BigDecimal krw(BigDecimal value) {
		return value == null ? null : value.setScale(0, RoundingMode.HALF_UP);
	}

	public static BigDecimal price(BigDecimal value) {
		return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
	}

	public static BigDecimal percent(BigDecimal value) {
		return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
	}

	/** Quantities without trailing zeros, never in exponent form (10, not 1E+1). */
	public static BigDecimal plain(BigDecimal value) {
		if (value == null) {
			return null;
		}
		BigDecimal stripped = value.stripTrailingZeros();
		return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
	}
}
