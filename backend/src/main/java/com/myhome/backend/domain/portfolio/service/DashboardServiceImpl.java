package com.myhome.backend.domain.portfolio.service;

import static com.myhome.backend.domain.portfolio.dto.PortfolioNumbers.krw;
import static com.myhome.backend.domain.portfolio.dto.PortfolioNumbers.percent;
import static com.myhome.backend.domain.portfolio.dto.PortfolioNumbers.plain;
import static com.myhome.backend.domain.portfolio.dto.PortfolioNumbers.price;

import com.myhome.backend.domain.portfolio.dto.DashboardDtos.AccountSummary;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.Allocation;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.DashboardResponse;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.Exposure;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.ExposureItem;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.Freshness;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.FxQuote;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.HistoryPoint;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.Holding;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.Maturity;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.Performance;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.PeriodChange;
import com.myhome.backend.domain.portfolio.dto.DashboardDtos.Summary;
import com.myhome.backend.domain.portfolio.entity.Account;
import com.myhome.backend.domain.portfolio.entity.Account.AccountType;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.PortfolioSnapshot;
import com.myhome.backend.domain.portfolio.entity.Price;
import com.myhome.backend.domain.portfolio.entity.MarketDataSource;
import com.myhome.backend.domain.portfolio.repository.PortfolioSnapshotRepository;
import com.myhome.backend.domain.portfolio.service.PortfolioValuator.AssetGroup;
import com.myhome.backend.domain.portfolio.service.PortfolioValuator.HoldingValue;
import com.myhome.backend.domain.portfolio.service.PortfolioValuator.Inputs;
import com.myhome.backend.domain.portfolio.service.PortfolioValuator.Result;
import com.myhome.backend.domain.portfolio.service.ReturnCalculator.CashFlow;
import com.myhome.backend.domain.portfolio.service.ReturnCalculator.Point;
import com.myhome.backend.domain.portfolio.service.ReturnCalculator.Xirr;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
	private static final BigDecimal AFTER_TAX = BigDecimal.ONE.subtract(new BigDecimal("0.154"));
	private static final int STALE_PRICE_DAYS = 7;
	private static final int MATURITY_WINDOW_DAYS = 90;

	private final PortfolioDataLoader dataLoader;
	private final PortfolioSnapshotRepository snapshotRepository;

	@Override
	public DashboardResponse getDashboard(String range) {
		ZonedDateTime calculatedAt = ZonedDateTime.now(ZONE);
		LocalDate today = calculatedAt.toLocalDate();
		LocalDate historyStart = historyStart(range, today);
		Inputs inputs = dataLoader.load();
		Result now = PortfolioValuator.valueAt(inputs, today);
		List<PortfolioSnapshot> snapshots = snapshotRepository.findAllByOrderBySnapshotDateAsc();
		List<CashFlow> flows = PortfolioValuator.scopeFlows(inputs, null, today);
		BigDecimal netContribution = flows.stream().map(CashFlow::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
		List<Holding> holdings = holdings(inputs, now.holdings(), calculatedAt);

		return new DashboardResponse(
				summary(now, snapshots, today, netContribution),
				performance(now, snapshots, today, flows, netContribution),
				allocation(now),
				exposure(now.holdings()),
				history(snapshots, now, historyStart, today),
				holdings,
				accounts(inputs, now),
				maturities(inputs, now, today),
				freshness(inputs, holdings, today),
				calculatedAt.toOffsetDateTime());
	}

	private static Summary summary(Result now, List<PortfolioSnapshot> snapshots, LocalDate today, BigDecimal netContribution) {
		LocalDate previousMonthEnd = today.withDayOfMonth(1).minusDays(1);
		PortfolioSnapshot monthBase = baseline(snapshots, previousMonthEnd);
		BigDecimal base = monthBase == null ? null : monthBase.getNetWorth();
		BigDecimal change = base == null ? null : now.netWorth().subtract(base);
		BigDecimal rate = base == null || base.signum() == 0
				? null
				: change.multiply(HUNDRED).divide(base.abs(), 8, RoundingMode.HALF_UP);
		return new Summary(krw(now.totalAssets()), krw(now.totalLiabilities()), krw(now.netWorth()), krw(change), percent(rate),
				periodChange(now, baseline(snapshots, today.minusDays(1)), netContribution),
				periodChange(now, monthBase, netContribution));
	}

	private static PortfolioSnapshot baseline(List<PortfolioSnapshot> snapshots, LocalDate through) {
		return snapshots.stream().filter(snapshot -> !snapshot.getSnapshotDate().isAfter(through))
				.max(Comparator.comparing(PortfolioSnapshot::getSnapshotDate)).orElse(null);
	}

	private static PeriodChange periodChange(Result now, PortfolioSnapshot base, BigDecimal cumulative) {
		if (base == null) {
			return null;
		}
		BigDecimal contributed = cumulative.subtract(base.getScopeCumulativeFlow());
		BigDecimal investmentChange = now.scopeValue().subtract(base.getScopeValue());
		return new PeriodChange(base.getSnapshotDate(), krw(now.netWorth().subtract(base.getNetWorth())),
				krw(investmentChange), krw(contributed), krw(investmentChange.subtract(contributed)));
	}

	private static Performance performance(Result now, List<PortfolioSnapshot> snapshots, LocalDate today,
			List<CashFlow> flows, BigDecimal netContribution) {
		Xirr xirr = ReturnCalculator.xirr(flows, now.scopeValue(), today);
		LocalDate since = flows.stream().map(CashFlow::date).min(Comparator.naturalOrder()).orElse(null);

		// TWR points: every snapshot, then today's live value (replacing a snapshot taken earlier today).
		List<Point> points = new ArrayList<>();
		BigDecimal lastCumulative = BigDecimal.ZERO;
		for (PortfolioSnapshot snapshot : snapshots) {
			if (!snapshot.getSnapshotDate().isBefore(today)) {
				break;
			}
			points.add(new Point(snapshot.getScopeValue(), snapshot.getScopeNetFlow()));
			lastCumulative = snapshot.getScopeCumulativeFlow();
		}
		// Everything contributed since the last frozen snapshot, including backdated entries made after it.
		points.add(new Point(now.scopeValue(), netContribution.subtract(lastCumulative)));
		BigDecimal twr = ReturnCalculator.twr(points);

		BigDecimal unrealized = now.holdings().stream()
				.map(holding -> holding.valuation().unrealizedKrw())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new Performance(
				xirr == null ? null : percent(xirr.ratePercent()),
				xirr == null ? null : xirr.annualized(),
				since,
				percent(twr),
				snapshots.isEmpty() ? null : snapshots.get(0).getSnapshotDate(),
				krw(unrealized),
				krw(now.ledger().realizedProfit()),
				krw(now.ledger().dividendIncome()),
				krw(netContribution));
	}

	private static List<Allocation> allocation(Result now) {
		BigDecimal total = now.totalAssets();
		return List.of(
				new Allocation(AssetGroup.CASH_EQUIVALENT, krw(now.cashEquivalent()), weight(now.cashEquivalent(), total)),
				new Allocation(AssetGroup.INVESTMENT, krw(now.investment()), weight(now.investment(), total)),
				new Allocation(AssetGroup.PENSION_INSURANCE, krw(now.pensionInsurance()), weight(now.pensionInsurance(), total)));
	}

	private static Exposure exposure(List<HoldingValue> holdings) {
		return new Exposure(
				exposureBy(holdings, holding -> holding.instrument().getExposureRegion().name()),
				exposureBy(holdings, holding -> currencyExposure(holding.instrument())),
				exposureBy(holdings, holding -> holding.instrument().getUnderlyingAssetClass().name()));
	}

	/**
	 * Real currency risk: a KRX-listed, unhedged S&P500 ETF is USD exposure; a hedged one ("(H)") is KRW.
	 * Multi-region funds are reported as MULTI.
	 */
	static String currencyExposure(Instrument instrument) {
		if (instrument.isForeignCurrency()) {
			return instrument.getTradingCurrency();
		}
		if (instrument.isFxHedged()) {
			return "KRW";
		}
		return switch (instrument.getExposureRegion()) {
			case KR -> "KRW";
			case US -> "USD";
			case JP -> "JPY";
			case CN -> "CNY";
			default -> "MULTI";
		};
	}

	private static List<ExposureItem> exposureBy(List<HoldingValue> holdings, Function<HoldingValue, String> key) {
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		holdings.forEach(holding -> totals.merge(key.apply(holding), holding.valuation().marketValueKrw(), BigDecimal::add));
		BigDecimal sum = totals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return totals.entrySet().stream()
				.sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
				.map(entry -> new ExposureItem(entry.getKey(), krw(entry.getValue()), weight(entry.getValue(), sum)))
				.toList();
	}

	private static List<HistoryPoint> history(
			List<PortfolioSnapshot> snapshots, Result now, LocalDate start, LocalDate today) {
		List<HistoryPoint> points = new ArrayList<>(snapshots.stream()
				.filter(snapshot -> start == null || !snapshot.getSnapshotDate().isBefore(start))
				.filter(snapshot -> snapshot.getSnapshotDate().isBefore(today))
				.map(snapshot -> new HistoryPoint(snapshot.getSnapshotDate(), krw(snapshot.getTotalAssets()),
						krw(snapshot.getTotalLiabilities()), krw(snapshot.getNetWorth())))
				.toList());
		points.add(new HistoryPoint(today, krw(now.totalAssets()), krw(now.totalLiabilities()), krw(now.netWorth())));
		return points;
	}

	private static List<Holding> holdings(Inputs inputs, List<HoldingValue> holdings, ZonedDateTime now) {
		BigDecimal total = holdings.stream()
				.map(holding -> holding.valuation().marketValueKrw())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return holdings.stream()
				.sorted(Comparator.comparing((HoldingValue holding) -> holding.valuation().marketValueKrw()).reversed())
				.map(holding -> new Holding(
						holding.instrument().getId(),
						holding.instrument().getSymbol(),
						holding.instrument().getName(),
						holding.instrument().getMarket(),
						holding.instrument().getTradingCurrency(),
						holding.instrument().getExposureRegion(),
						holding.instrument().isFxHedged(),
						holding.account().getId(),
						holding.account().getName(),
						plain(holding.position().quantity()),
						price(holding.position().avgPriceLocal()),
						price(holding.closePrice()),
						holding.priceDate(),
						priceStale(inputs, holding, now),
						quote(inputs, holding) == null ? null : quote(inputs, holding).getSource(),
						collectedAt(inputs, holding),
						krw(holding.valuation().marketValueKrw()),
						krw(holding.position().costKrw()),
						krw(holding.valuation().unrealizedKrw()),
						percent(holding.valuation().returnRatePercent()),
						krw(holding.valuation().priceEffectKrw()),
						krw(holding.valuation().fxEffectKrw()),
						weight(holding.valuation().marketValueKrw(), total)))
				.toList();
	}

	private static Price quote(Inputs inputs, HoldingValue holding) {
		return holding.priceDate() == null ? null
				: inputs.pricesByInstrument().get(holding.instrument().getId()).get(holding.priceDate());
	}

	private static OffsetDateTime collectedAt(Inputs inputs, HoldingValue holding) {
		Price price = quote(inputs, holding);
		// Stored wall times share the existing JVM clock convention with snapshot and ledger timestamps.
		return price == null || price.getCapturedAt() == null ? null
				: price.getCapturedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZONE).toOffsetDateTime();
	}

	private static boolean priceStale(Inputs inputs, HoldingValue holding, ZonedDateTime now) {
		if (holding.priceDate() == null || holding.priceDate().isBefore(now.toLocalDate().minusDays(STALE_PRICE_DAYS))) {
			return true;
		}
		Price price = quote(inputs, holding);
		OffsetDateTime captured = collectedAt(inputs, holding);
		return price.getSource() == MarketDataSource.KIS && PortfolioMarketHours.collectionDelayed(
				holding.instrument().getMarket(), captured == null ? null : captured.toZonedDateTime(), now);
	}

	private static List<AccountSummary> accounts(Inputs inputs, Result now) {
		return inputs.accounts().stream()
				.filter(account -> account.getClosedAt() == null)
				.map(account -> new AccountSummary(
						account.getId(),
						account.getName(),
						account.getInstitution(),
						account.getAccountType(),
						PortfolioValuator.groupOf(account),
						krw(now.accountValues().getOrDefault(account.getId(), BigDecimal.ZERO))))
				.toList();
	}

	private static List<Maturity> maturities(Inputs inputs, Result now, LocalDate today) {
		return inputs.accounts().stream()
				.filter(account -> account.getClosedAt() == null && account.getMaturityDate() != null)
				.filter(account -> !account.getMaturityDate().isBefore(today)
						&& !account.getMaturityDate().isAfter(today.plusDays(MATURITY_WINDOW_DAYS)))
				.sorted(Comparator.comparing(Account::getMaturityDate))
				.map(account -> new Maturity(
						account.getId(),
						account.getName(),
						account.getAccountType(),
						account.getMaturityDate(),
						ChronoUnit.DAYS.between(today, account.getMaturityDate()),
						krw(expectedInterestAfterTax(account, now.accountValues().get(account.getId())))))
				.toList();
	}

	/** Simple-interest estimate after 15.4% withholding; null when the account lacks rate or dates. */
	static BigDecimal expectedInterestAfterTax(Account account, BigDecimal balance) {
		if (account.getInterestRate() == null || account.getOpenedAt() == null || account.getMaturityDate() == null) {
			return null;
		}
		BigDecimal annualRate = account.getInterestRate().movePointLeft(2);
		if (account.getAccountType() == AccountType.TIME_DEPOSIT && balance != null) {
			long days = ChronoUnit.DAYS.between(account.getOpenedAt(), account.getMaturityDate());
			return balance.multiply(annualRate).multiply(BigDecimal.valueOf(days))
					.divide(BigDecimal.valueOf(365), 8, RoundingMode.HALF_UP)
					.multiply(AFTER_TAX);
		}
		if (account.getAccountType() == AccountType.INSTALLMENT_SAVINGS && account.getMonthlyPayment() != null) {
			long months = ChronoUnit.MONTHS.between(account.getOpenedAt(), account.getMaturityDate());
			return account.getMonthlyPayment().multiply(annualRate)
					.multiply(BigDecimal.valueOf(months * (months + 1) / 2))
					.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP)
					.multiply(AFTER_TAX);
		}
		return null;
	}

	private static Freshness freshness(Inputs inputs, List<Holding> holdings, LocalDate today) {
		List<LocalDate> priceDates = holdings.stream().map(Holding::priceDate).filter(Objects::nonNull).toList();
		int stale = (int) holdings.stream().filter(Holding::priceStale).count();
		List<FxQuote> fxRates = inputs.fxByCurrency().entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> {
					Map.Entry<LocalDate, BigDecimal> latest = entry.getValue().floorEntry(today);
					return latest == null ? null : new FxQuote(entry.getKey(), plain(latest.getValue()), latest.getKey());
				})
				.filter(Objects::nonNull)
				.toList();
		return new Freshness(
				priceDates.stream().min(Comparator.naturalOrder()).orElse(null),
				priceDates.stream().max(Comparator.naturalOrder()).orElse(null),
				stale,
				holdings.stream().map(Holding::priceCollectedAt).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null),
				fxRates);
	}

	private static LocalDate historyStart(String range, LocalDate today) {
		return switch (range == null ? "1Y" : range.toUpperCase()) {
			case "1M" -> today.minusMonths(1);
			case "3M" -> today.minusMonths(3);
			case "6M" -> today.minusMonths(6);
			case "1Y" -> today.minusYears(1);
			case "ALL" -> null;
			default -> throw new LedgerValidationException("range는 1M, 3M, 6M, 1Y, ALL 중 하나입니다");
		};
	}

	private static BigDecimal weight(BigDecimal part, BigDecimal total) {
		return total.signum() <= 0 ? null : percent(part.multiply(HUNDRED).divide(total, 8, RoundingMode.HALF_UP));
	}
}
