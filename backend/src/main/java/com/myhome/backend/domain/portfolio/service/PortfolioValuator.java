package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.entity.Account;
import com.myhome.backend.domain.portfolio.entity.Account.AccountType;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction;
import com.myhome.backend.domain.portfolio.entity.Price;
import com.myhome.backend.domain.portfolio.service.HoldingsCalculator.LedgerState;
import com.myhome.backend.domain.portfolio.service.HoldingsCalculator.Position;
import com.myhome.backend.domain.portfolio.service.HoldingsCalculator.Valuation;
import com.myhome.backend.domain.portfolio.service.ReturnCalculator.CashFlow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Values the whole portfolio as of a date from the ledger, prices, FX and manual valuations. Pure; no I/O.
 * ponytail: every call replays the full ledger in memory. Fine for a personal ledger (thousands of rows);
 * add incremental replay if snapshot rebuilds over long ranges get slow.
 */
public final class PortfolioValuator {

	/** Dashboard groups. Pension/IRP accounts count as a whole (withdrawal-restricted), others by what they hold. */
	public enum AssetGroup { CASH_EQUIVALENT, INVESTMENT, PENSION_INSURANCE, LIABILITY }

	private static final Set<AccountType> PENSION_ACCOUNTS = Set.of(AccountType.PENSION_SAVINGS, AccountType.IRP);

	private PortfolioValuator() {
	}

	/** All data needed for valuation, loaded once. Transactions must be in replay order. */
	public record Inputs(
			List<Account> accounts,
			Map<Long, Instrument> instruments,
			List<LedgerTransaction> transactions,
			Map<Long, NavigableMap<LocalDate, Price>> pricesByInstrument,
			Map<String, NavigableMap<LocalDate, BigDecimal>> fxByCurrency,
			Map<Long, NavigableMap<LocalDate, BigDecimal>> valuationsByAccount
	) {
	}

	/** priceDate is null when no price exists yet: the holding is then valued at its average cost. */
	public record HoldingValue(
			Position position,
			Account account,
			Instrument instrument,
			BigDecimal closePrice,
			LocalDate priceDate,
			BigDecimal fxRate,
			Valuation valuation
	) {
	}

	public record Result(
			LocalDate asOf,
			BigDecimal cashEquivalent,
			BigDecimal investment,
			BigDecimal pensionInsurance,
			BigDecimal totalLiabilities,
			BigDecimal scopeValue,
			LocalDate priceAsOf,
			List<HoldingValue> holdings,
			Map<Long, BigDecimal> accountValues,
			LedgerState ledger
	) {
		public BigDecimal totalAssets() {
			return cashEquivalent.add(investment).add(pensionInsurance);
		}

		public BigDecimal netWorth() {
			return totalAssets().subtract(totalLiabilities);
		}
	}

	public static AssetGroup groupOf(Account account) {
		AccountType type = account.getAccountType();
		if (type == AccountType.LOAN) {
			return AssetGroup.LIABILITY;
		}
		if (PENSION_ACCOUNTS.contains(type) || type == AccountType.INSURANCE) {
			return AssetGroup.PENSION_INSURANCE;
		}
		return type.isInvestment() ? AssetGroup.INVESTMENT : AssetGroup.CASH_EQUIVALENT;
	}

	public static Result valueAt(Inputs inputs, LocalDate date) {
		LedgerState ledger = HoldingsCalculator.replay(inputs.transactions().stream()
				.filter(transaction -> !transaction.getTradeDate().isAfter(date))
				.toList());
		Map<Long, Account> accounts = inputs.accounts().stream()
				.collect(Collectors.toMap(Account::getId, Function.identity()));

		BigDecimal cash = BigDecimal.ZERO;
		BigDecimal investment = BigDecimal.ZERO;
		BigDecimal pension = BigDecimal.ZERO;
		BigDecimal liabilities = BigDecimal.ZERO;
		BigDecimal scope = BigDecimal.ZERO;
		LocalDate priceAsOf = null;
		List<HoldingValue> holdings = new ArrayList<>();
		Map<Long, BigDecimal> accountValues = new LinkedHashMap<>();

		for (Position position : ledger.positions().values()) {
			if (position.quantity().signum() <= 0) {
				continue;
			}
			Account account = accounts.get(position.accountId());
			Instrument instrument = inputs.instruments().get(position.instrumentId());

			Map.Entry<LocalDate, Price> price = floor(inputs.pricesByInstrument().get(instrument.getId()), date);
			BigDecimal close = price == null ? position.avgPriceLocal() : price.getValue().getClosePrice();
			BigDecimal fxRate = fxRateFor(inputs, instrument, position, date);
			Valuation valuation = HoldingsCalculator.value(position, close, fxRate, instrument.isForeignCurrency());

			if (price != null && (priceAsOf == null || price.getKey().isBefore(priceAsOf))) {
				priceAsOf = price.getKey();
			}
			holdings.add(new HoldingValue(position, account, instrument, close,
					price == null ? null : price.getKey(), fxRate, valuation));

			BigDecimal marketValue = valuation.marketValueKrw();
			accountValues.merge(account.getId(), marketValue, BigDecimal::add);
			if (PENSION_ACCOUNTS.contains(account.getAccountType())) {
				pension = pension.add(marketValue);
			} else {
				investment = investment.add(marketValue);
			}
			if (account.getAccountType().isInvestment()) {
				scope = scope.add(marketValue);
			}
		}

		for (Account account : inputs.accounts()) {
			AccountType type = account.getAccountType();
			if (type == AccountType.LOAN) {
				BigDecimal balance = ledger.liabilityByAccount().getOrDefault(account.getId(), BigDecimal.ZERO);
				liabilities = liabilities.add(balance);
				accountValues.put(account.getId(), balance);
			} else if (type == AccountType.INSURANCE) {
				Map.Entry<LocalDate, BigDecimal> valuation = floor(inputs.valuationsByAccount().get(account.getId()), date);
				BigDecimal value = valuation == null ? BigDecimal.ZERO : valuation.getValue();
				pension = pension.add(value);
				accountValues.put(account.getId(), value);
			} else {
				BigDecimal balance = ledger.cashByAccount().getOrDefault(account.getId(), BigDecimal.ZERO);
				if (PENSION_ACCOUNTS.contains(type)) {
					pension = pension.add(balance);
				} else {
					cash = cash.add(balance);
				}
				if (type.isInvestment()) {
					scope = scope.add(balance);
				}
				accountValues.merge(account.getId(), balance, BigDecimal::add);
			}
		}

		return new Result(date, cash, investment, pension, liabilities, scope, priceAsOf, holdings, accountValues, ledger);
	}

	/**
	 * External contributions into the investment scope in (fromExclusive, toInclusive]: + in, - out.
	 * OPENING counts as money put in on the start date. Transfers inside the scope (e.g. brokerage to ISA) are ignored.
	 */
	public static List<CashFlow> scopeFlows(Inputs inputs, LocalDate fromExclusive, LocalDate toInclusive) {
		Set<Long> scopeAccounts = inputs.accounts().stream()
				.filter(account -> account.getAccountType().isInvestment())
				.map(Account::getId)
				.collect(Collectors.toSet());
		Map<UUID, List<LedgerTransaction>> transferLegs = inputs.transactions().stream()
				.filter(transaction -> transaction.getTransferGroupId() != null)
				.collect(Collectors.groupingBy(LedgerTransaction::getTransferGroupId));

		List<CashFlow> flows = new ArrayList<>();
		for (LedgerTransaction transaction : inputs.transactions()) {
			LocalDate date = transaction.getTradeDate();
			if ((fromExclusive != null && !date.isAfter(fromExclusive)) || date.isAfter(toInclusive)
					|| !scopeAccounts.contains(transaction.getAccountId())) {
				continue;
			}
			BigDecimal amount = transaction.getAmount();
			BigDecimal signed = switch (transaction.getType()) {
				case OPENING, DEPOSIT -> amount;
				case WITHDRAW -> amount.negate();
				case TRANSFER_IN -> crossesScope(transaction, transferLegs, scopeAccounts) ? amount : null;
				case TRANSFER_OUT -> crossesScope(transaction, transferLegs, scopeAccounts) ? amount.negate() : null;
				default -> null; // trades, dividends, interest, fees: returns inside the scope, not contributions
			};
			if (signed != null) {
				flows.add(new CashFlow(date, signed));
			}
		}
		return flows;
	}

	private static boolean crossesScope(
			LedgerTransaction leg, Map<UUID, List<LedgerTransaction>> transferLegs, Set<Long> scopeAccounts) {
		return transferLegs.getOrDefault(leg.getTransferGroupId(), List.of()).stream()
				.filter(other -> !other.getId().equals(leg.getId()))
				.noneMatch(other -> scopeAccounts.contains(other.getAccountId()));
	}

	/** Latest FX on or before the date; falls back to the position's own average purchase FX. */
	private static BigDecimal fxRateFor(Inputs inputs, Instrument instrument, Position position, LocalDate date) {
		if (!instrument.isForeignCurrency()) {
			return BigDecimal.ONE;
		}
		Map.Entry<LocalDate, BigDecimal> fx = floor(inputs.fxByCurrency().get(instrument.getTradingCurrency()), date);
		if (fx != null) {
			return fx.getValue();
		}
		return position.costLocal().signum() == 0
				? BigDecimal.ONE
				: position.costKrw().divide(position.costLocal(), HoldingsCalculator.SCALE, RoundingMode.HALF_UP);
	}

	private static <T> Map.Entry<LocalDate, T> floor(NavigableMap<LocalDate, T> series, LocalDate date) {
		return series == null ? null : series.floorEntry(date);
	}
}
