package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.entity.LedgerTransaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure ledger replay: positions (moving-average cost, as Korean brokers display it), cash and loan balances.
 * Input must be in replay order (trade date, then id). Money stays BigDecimal; rounding happens only in responses.
 */
public final class HoldingsCalculator {

	static final int SCALE = 8;
	private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private HoldingsCalculator() {
	}

	public record PositionKey(Long accountId, Long instrumentId) {
	}

	/** costKrw includes buy fees; costLocal is quantity x price in the trading currency. */
	public record Position(
			Long accountId,
			Long instrumentId,
			BigDecimal quantity,
			BigDecimal costKrw,
			BigDecimal costLocal,
			BigDecimal realizedKrw,
			BigDecimal dividendsKrw
	) {
		public BigDecimal avgPriceLocal() {
			return quantity.signum() == 0 ? null : costLocal.divide(quantity, SCALE, ROUNDING);
		}
	}

	public record LedgerState(
			Map<PositionKey, Position> positions,
			Map<Long, BigDecimal> cashByAccount,
			Map<Long, BigDecimal> liabilityByAccount
	) {
		public BigDecimal realizedProfit() {
			return positions.values().stream().map(Position::realizedKrw).reduce(BigDecimal.ZERO, BigDecimal::add);
		}

		public BigDecimal dividendIncome() {
			return positions.values().stream().map(Position::dividendsKrw).reduce(BigDecimal.ZERO, BigDecimal::add);
		}
	}

	/** priceEffect + fxEffect = unrealized; both null for KRW instruments. */
	public record Valuation(
			BigDecimal marketValueKrw,
			BigDecimal unrealizedKrw,
			BigDecimal returnRatePercent,
			BigDecimal priceEffectKrw,
			BigDecimal fxEffectKrw
	) {
	}

	public static LedgerState replay(List<LedgerTransaction> transactions) {
		Map<PositionKey, MutablePosition> positions = new LinkedHashMap<>();
		Map<Long, BigDecimal> cash = new LinkedHashMap<>();
		Map<Long, BigDecimal> liabilities = new LinkedHashMap<>();

		for (LedgerTransaction transaction : transactions) {
			Long accountId = transaction.getAccountId();
			BigDecimal amount = transaction.getAmount();

			switch (transaction.getType()) {
				case OPENING -> {
					if (transaction.getInstrumentId() == null) {
						cash.merge(accountId, amount, BigDecimal::add);
					} else {
						position(positions, transaction).buy(transaction.getQuantity(), transaction.getPrice(), amount);
					}
				}
				case BUY -> {
					position(positions, transaction).buy(transaction.getQuantity(), transaction.getPrice(), amount);
					cash.merge(accountId, amount.negate(), BigDecimal::add);
				}
				case SELL -> {
					position(positions, transaction).sell(transaction);
					cash.merge(accountId, amount, BigDecimal::add);
				}
				case DIVIDEND -> {
					position(positions, transaction).dividends = position(positions, transaction).dividends.add(amount);
					cash.merge(accountId, amount, BigDecimal::add);
				}
				case DEPOSIT, TRANSFER_IN, INTEREST -> cash.merge(accountId, amount, BigDecimal::add);
				case WITHDRAW, TRANSFER_OUT, FEE, TAX -> cash.merge(accountId, amount.negate(), BigDecimal::add);
				case LOAN_DRAW -> liabilities.merge(accountId, amount, BigDecimal::add);
				case LOAN_REPAY -> {
					BigDecimal balance = liabilities.getOrDefault(accountId, BigDecimal.ZERO);
					if (amount.compareTo(balance) > 0) {
						throw new LedgerValidationException("상환액이 대출 잔액을 초과합니다 (%s, 잔액 %s, 상환 %s)"
								.formatted(transaction.getTradeDate(), balance.toPlainString(), amount.toPlainString()));
					}
					liabilities.put(accountId, balance.subtract(amount));
				}
			}
		}

		Map<PositionKey, Position> result = new LinkedHashMap<>();
		positions.forEach((key, position) -> result.put(key, position.freeze(key)));
		return new LedgerState(result, cash, liabilities);
	}

	public static Valuation value(Position position, BigDecimal closePrice, BigDecimal fxRate, boolean foreignCurrency) {
		BigDecimal marketValue = position.quantity().multiply(closePrice).multiply(fxRate);
		BigDecimal unrealized = marketValue.subtract(position.costKrw());
		BigDecimal returnRate = position.costKrw().signum() == 0
				? null
				: unrealized.multiply(HUNDRED).divide(position.costKrw(), SCALE, ROUNDING);

		if (!foreignCurrency) {
			return new Valuation(marketValue, unrealized, returnRate, null, null);
		}
		// Cost re-priced at today's FX minus cost at purchase FX. Buy fees (inside costKrw) land here too.
		BigDecimal fxEffect = position.costLocal().multiply(fxRate).subtract(position.costKrw());
		return new Valuation(marketValue, unrealized, returnRate, unrealized.subtract(fxEffect), fxEffect);
	}

	private static MutablePosition position(Map<PositionKey, MutablePosition> positions, LedgerTransaction transaction) {
		return positions.computeIfAbsent(
				new PositionKey(transaction.getAccountId(), transaction.getInstrumentId()),
				key -> new MutablePosition());
	}

	private static final class MutablePosition {

		private BigDecimal quantity = BigDecimal.ZERO;
		private BigDecimal costKrw = BigDecimal.ZERO;
		private BigDecimal costLocal = BigDecimal.ZERO;
		private BigDecimal realized = BigDecimal.ZERO;
		private BigDecimal dividends = BigDecimal.ZERO;

		void buy(BigDecimal buyQuantity, BigDecimal price, BigDecimal settledKrw) {
			quantity = quantity.add(buyQuantity);
			costKrw = costKrw.add(settledKrw);
			costLocal = costLocal.add(buyQuantity.multiply(price));
		}

		void sell(LedgerTransaction transaction) {
			BigDecimal sellQuantity = transaction.getQuantity();
			if (sellQuantity.compareTo(quantity) > 0) {
				throw new LedgerValidationException("매도 수량이 보유 수량을 초과합니다 (%s, 종목 #%d: 보유 %s, 매도 %s)"
						.formatted(transaction.getTradeDate(), transaction.getInstrumentId(),
								quantity.stripTrailingZeros().toPlainString(), sellQuantity.stripTrailingZeros().toPlainString()));
			}

			if (sellQuantity.compareTo(quantity) == 0) {
				realized = realized.add(transaction.getAmount()).subtract(costKrw);
				quantity = BigDecimal.ZERO;
				costKrw = BigDecimal.ZERO;
				costLocal = BigDecimal.ZERO;
				return;
			}

			BigDecimal soldCostKrw = costKrw.multiply(sellQuantity).divide(quantity, SCALE, ROUNDING);
			BigDecimal soldCostLocal = costLocal.multiply(sellQuantity).divide(quantity, SCALE, ROUNDING);
			// Sell proceeds are the settled amount (net of fee and tax), matching the broker statement.
			realized = realized.add(transaction.getAmount()).subtract(soldCostKrw);
			quantity = quantity.subtract(sellQuantity);
			costKrw = costKrw.subtract(soldCostKrw);
			costLocal = costLocal.subtract(soldCostLocal);
		}

		Position freeze(PositionKey key) {
			return new Position(key.accountId(), key.instrumentId(), quantity, costKrw, costLocal, realized, dividends);
		}
	}
}
