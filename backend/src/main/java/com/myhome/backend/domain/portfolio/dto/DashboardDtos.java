package com.myhome.backend.domain.portfolio.dto;

import com.myhome.backend.domain.portfolio.entity.Account.AccountType;
import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import com.myhome.backend.domain.portfolio.entity.Instrument.Region;
import com.myhome.backend.domain.portfolio.entity.MarketDataSource;
import com.myhome.backend.domain.portfolio.service.PortfolioValuator.AssetGroup;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** One response for the whole owner dashboard. KRW amounts are whole won, rates are percent with 2 decimals. */
public final class DashboardDtos {

	private DashboardDtos() {
	}

	public record DashboardResponse(
			Summary summary,
			Performance performance,
			List<Allocation> allocation,
			Exposure exposure,
			List<HistoryPoint> history,
			List<Holding> holdings,
			List<AccountSummary> accounts,
			List<Maturity> upcomingMaturities,
			Freshness freshness,
			OffsetDateTime calculatedAt
	) {
	}

	/** Month-over-month compares with the last snapshot of the previous month; null without one. */
	public record Summary(
			BigDecimal totalAssets,
			BigDecimal totalLiabilities,
			BigDecimal netWorth,
			BigDecimal monthOverMonthChange,
			BigDecimal monthOverMonthRate,
			PeriodChange dayChange,
			PeriodChange monthChange
	) {
	}

	/** Investment changes exclude contributions using frozen scope_cumulative_flow, including late entries. */
	public record PeriodChange(LocalDate baseDate, BigDecimal netWorthChange,
			BigDecimal investmentChange, BigDecimal netContribution, BigDecimal investmentGain) {
	}

	/** XIRR since the first contribution (not annualized under a year); TWR since the first snapshot. */
	public record Performance(
			BigDecimal xirr,
			Boolean xirrAnnualized,
			LocalDate since,
			BigDecimal twr,
			LocalDate twrSince,
			BigDecimal unrealizedProfit,
			BigDecimal realizedProfit,
			BigDecimal dividendIncome,
			BigDecimal netContribution
	) {
	}

	public record Allocation(AssetGroup group, BigDecimal amount, BigDecimal weightPercent) {
	}

	public record Exposure(List<ExposureItem> byRegion, List<ExposureItem> byCurrency, List<ExposureItem> byAssetClass) {
	}

	public record ExposureItem(String key, BigDecimal amount, BigDecimal weightPercent) {
	}

	public record HistoryPoint(LocalDate date, BigDecimal totalAssets, BigDecimal totalLiabilities, BigDecimal netWorth) {
	}

	/** priceEffect / fxEffect only for foreign-currency holdings; priceDate null means valued at average cost. */
	public record Holding(
			Long instrumentId,
			String symbol,
			String name,
			Market market,
			String tradingCurrency,
			Region exposureRegion,
			boolean fxHedged,
			Long accountId,
			String accountName,
			BigDecimal quantity,
			BigDecimal avgPriceLocal,
			BigDecimal closePrice,
			LocalDate priceDate,
			boolean priceStale,
			MarketDataSource priceSource,
			OffsetDateTime priceCollectedAt,
			BigDecimal marketValueKrw,
			BigDecimal costKrw,
			BigDecimal unrealizedProfit,
			BigDecimal returnRate,
			BigDecimal priceEffect,
			BigDecimal fxEffect,
			BigDecimal weightPercent
	) {
	}

	/** valueKrw is the outstanding balance for loans (group LIABILITY). */
	public record AccountSummary(
			Long id,
			String name,
			String institution,
			AccountType accountType,
			AssetGroup group,
			BigDecimal valueKrw
	) {
	}

	/** expectedInterestAfterTax: estimate (15.4% withholding), null when rate or dates are missing. */
	public record Maturity(
			Long accountId,
			String name,
			AccountType accountType,
			LocalDate maturityDate,
			long daysLeft,
			BigDecimal expectedInterestAfterTax
	) {
	}

	public record Freshness(
			LocalDate oldestPriceDate,
			LocalDate latestPriceDate,
			int staleHoldings,
			OffsetDateTime latestCollectedAt,
			List<FxQuote> fxRates
	) {
	}

	public record FxQuote(String currency, BigDecimal rateToKrw, LocalDate rateDate) {
	}
}
