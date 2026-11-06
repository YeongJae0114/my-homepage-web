package com.myhome.backend.domain.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Money-weighted (XIRR) and time-weighted (TWR) returns. Rates are solved in double (they are not money);
 * results come back as percentages.
 */
public final class ReturnCalculator {

	private static final double DAYS_PER_YEAR = 365.0;
	private static final double TOLERANCE = 1e-7;
	private static final int MAX_ITERATIONS = 100;

	private ReturnCalculator() {
	}

	/** amount > 0: money the owner put into the scope; amount < 0: money taken out. */
	public record CashFlow(LocalDate date, BigDecimal amount) {
	}

	/** annualized=false when the history is shorter than a year: then the rate is the plain period return. */
	public record Xirr(BigDecimal ratePercent, boolean annualized) {
	}

	/** One valuation point: value at the point, and net contributions since the previous point. */
	public record Point(BigDecimal value, BigDecimal netFlowSincePrevious) {
	}

	/** Null when undefined (no contributions, nothing to value, or no solution). */
	public static Xirr xirr(List<CashFlow> contributions, BigDecimal currentValue, LocalDate asOf) {
		if (contributions.isEmpty() || currentValue == null) {
			return null;
		}
		LocalDate start = contributions.stream().map(CashFlow::date).min(Comparator.naturalOrder()).orElseThrow();
		long days = ChronoUnit.DAYS.between(start, asOf);
		if (days <= 0) {
			return null;
		}

		// Investor's view: contributions are outflows, today's value is the final inflow.
		int size = contributions.size() + 1;
		double[] amounts = new double[size];
		double[] years = new double[size];
		for (int i = 0; i < contributions.size(); i++) {
			amounts[i] = -contributions.get(i).amount().doubleValue();
			years[i] = ChronoUnit.DAYS.between(start, contributions.get(i).date()) / DAYS_PER_YEAR;
		}
		amounts[size - 1] = currentValue.doubleValue();
		years[size - 1] = days / DAYS_PER_YEAR;

		double rate = solve(amounts, years);
		if (Double.isNaN(rate)) {
			return null;
		}
		if (days < DAYS_PER_YEAR) {
			// A few months of +5% must not be shown as +20% a year.
			return new Xirr(percent(Math.pow(1 + rate, days / DAYS_PER_YEAR) - 1), false);
		}
		return new Xirr(percent(rate), true);
	}

	/**
	 * Chain-linked Modified Dietz: r = (V1 - V0 - F) / (V0 + F/2), flows assumed mid-period.
	 * With daily points the mid-period assumption is practically exact. Null with fewer than two points.
	 */
	public static BigDecimal twr(List<Point> points) {
		if (points.size() < 2) {
			return null;
		}
		double growth = 1;
		for (int i = 1; i < points.size(); i++) {
			double previous = points.get(i - 1).value().doubleValue();
			double flow = points.get(i).netFlowSincePrevious().doubleValue();
			double denominator = previous + flow / 2;
			if (denominator <= 0) {
				continue; // empty start (nothing invested yet): no return for this segment
			}
			growth *= 1 + (points.get(i).value().doubleValue() - previous - flow) / denominator;
		}
		return percent(growth - 1);
	}

	private static double solve(double[] amounts, double[] years) {
		boolean hasOutflow = false;
		boolean hasInflow = false;
		for (double amount : amounts) {
			hasOutflow |= amount < 0;
			hasInflow |= amount > 0;
		}
		if (!hasOutflow || !hasInflow) {
			return Double.NaN;
		}

		double rate = 0.1;
		for (int i = 0; i < MAX_ITERATIONS; i++) {
			double value = npv(amounts, years, rate);
			double derivative = 0;
			for (int j = 0; j < amounts.length; j++) {
				derivative -= years[j] * amounts[j] / Math.pow(1 + rate, years[j] + 1);
			}
			if (derivative == 0) {
				break;
			}
			double next = rate - value / derivative;
			if (next <= -0.999999 || Double.isNaN(next)) {
				break;
			}
			if (Math.abs(next - rate) < TOLERANCE) {
				return next;
			}
			rate = next;
		}
		return bisect(amounts, years);
	}

	private static double bisect(double[] amounts, double[] years) {
		double low = -0.99;
		double high = 10;
		double lowValue = npv(amounts, years, low);
		if (lowValue * npv(amounts, years, high) > 0) {
			return Double.NaN;
		}
		for (int i = 0; i < 200; i++) {
			double middle = (low + high) / 2;
			double middleValue = npv(amounts, years, middle);
			if (Math.abs(middleValue) < TOLERANCE || (high - low) / 2 < TOLERANCE) {
				return middle;
			}
			if (Math.signum(middleValue) == Math.signum(lowValue)) {
				low = middle;
				lowValue = middleValue;
			} else {
				high = middle;
			}
		}
		return (low + high) / 2;
	}

	private static double npv(double[] amounts, double[] years, double rate) {
		double total = 0;
		for (int i = 0; i < amounts.length; i++) {
			total += amounts[i] / Math.pow(1 + rate, years[i]);
		}
		return total;
	}

	private static BigDecimal percent(double ratio) {
		return BigDecimal.valueOf(ratio * 100).setScale(HoldingsCalculator.SCALE, RoundingMode.HALF_UP);
	}
}
