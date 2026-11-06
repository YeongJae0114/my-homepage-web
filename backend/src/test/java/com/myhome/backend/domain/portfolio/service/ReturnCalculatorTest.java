package com.myhome.backend.domain.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.myhome.backend.domain.portfolio.service.ReturnCalculator.CashFlow;
import com.myhome.backend.domain.portfolio.service.ReturnCalculator.Point;
import com.myhome.backend.domain.portfolio.service.ReturnCalculator.Xirr;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReturnCalculatorTest {

	@Test
	void xirrMatchesSpreadsheetReference() {
		// Excel XIRR documentation example: -10000, 2750, 4250, 3250, 2750 -> 37.336%
		List<CashFlow> contributions = List.of(
				flow("2008-01-01", "10000"),
				flow("2008-03-01", "-2750"),
				flow("2008-10-30", "-4250"),
				flow("2009-02-15", "-3250"));

		Xirr xirr = ReturnCalculator.xirr(contributions, new BigDecimal("2750"), LocalDate.parse("2009-04-01"));

		assertThat(xirr.annualized()).isTrue();
		assertThat(xirr.ratePercent().doubleValue()).isCloseTo(37.3362535, within(1e-4));
	}

	@Test
	void historyShorterThanAYearIsNotAnnualized() {
		Xirr xirr = ReturnCalculator.xirr(
				List.of(flow("2026-01-01", "1000000")), new BigDecimal("1050000"), LocalDate.parse("2026-07-01"));

		assertThat(xirr.annualized()).isFalse();
		assertThat(xirr.ratePercent().doubleValue()).isCloseTo(5.0, within(1e-6));
	}

	@Test
	void xirrIsUndefinedWithoutContributionsOrTime() {
		assertThat(ReturnCalculator.xirr(List.of(), BigDecimal.TEN, LocalDate.parse("2026-01-01"))).isNull();
		assertThat(ReturnCalculator.xirr(
				List.of(flow("2026-01-01", "100")), new BigDecimal("100"), LocalDate.parse("2026-01-01"))).isNull();
	}

	@Test
	void twrRemovesTheEffectOfContributions() {
		BigDecimal twr = ReturnCalculator.twr(List.of(
				point("1000", "0"),
				point("1100", "0"),      // +10%
				point("1650", "500")));  // (1650 - 1100 - 500) / (1100 + 250) = +3.7037%

		assertThat(twr.doubleValue()).isCloseTo((1.1 * (1 + 50.0 / 1350) - 1) * 100, within(1e-6));
	}

	@Test
	void twrSkipsTheEmptyStartAndNeedsTwoPoints() {
		assertThat(ReturnCalculator.twr(List.of(point("1000", "0")))).isNull();
		// First deposit lands in a segment that starts at zero: only the later growth counts.
		BigDecimal twr = ReturnCalculator.twr(List.of(point("0", "0"), point("1000", "1000"), point("1200", "0")));
		assertThat(twr.doubleValue()).isCloseTo(20.0, within(1e-6));
	}

	private static CashFlow flow(String date, String amount) {
		return new CashFlow(LocalDate.parse(date), new BigDecimal(amount));
	}

	private static Point point(String value, String flow) {
		return new Point(new BigDecimal(value), new BigDecimal(flow));
	}
}
