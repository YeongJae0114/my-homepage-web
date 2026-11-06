package com.myhome.backend.domain.portfolio.service;

import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.BUY;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.DIVIDEND;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.LOAN_DRAW;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.LOAN_REPAY;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.OPENING;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.SELL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myhome.backend.domain.portfolio.entity.LedgerTransaction;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType;
import com.myhome.backend.domain.portfolio.service.HoldingsCalculator.LedgerState;
import com.myhome.backend.domain.portfolio.service.HoldingsCalculator.Position;
import com.myhome.backend.domain.portfolio.service.HoldingsCalculator.PositionKey;
import com.myhome.backend.domain.portfolio.service.HoldingsCalculator.Valuation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HoldingsCalculatorTest {

	private static final long ACCOUNT = 1L;
	private static final long ETF = 10L;
	private static final LocalDate DAY = LocalDate.of(2026, 1, 2);

	@Test
	void movingAverageCostAndRealizedProfit() {
		LedgerState state = HoldingsCalculator.replay(List.of(
				cash(OPENING, "300000"),
				trade(BUY, "10", "10000", "1", "100000"),
				trade(BUY, "10", "12000", "1", "120000"),
				trade(SELL, "5", "13000", "1", "65000"),
				dividend("3000")));

		Position position = state.positions().get(new PositionKey(ACCOUNT, ETF));
		assertThat(position.quantity()).isEqualByComparingTo("15");
		assertThat(position.avgPriceLocal()).isEqualByComparingTo("11000");
		assertThat(position.costKrw()).isEqualByComparingTo("165000");
		assertThat(position.realizedKrw()).isEqualByComparingTo("10000"); // 65,000 - 5 x 11,000
		assertThat(state.realizedProfit()).isEqualByComparingTo("10000");
		assertThat(state.dividendIncome()).isEqualByComparingTo("3000");
		assertThat(state.cashByAccount().get(ACCOUNT)).isEqualByComparingTo("148000"); // 300k - 220k + 65k + 3k
	}

	@Test
	void sellingEverythingClearsCostExactly() {
		LedgerState state = HoldingsCalculator.replay(List.of(
				trade(BUY, "3", "10000", "1", "30015"),
				trade(SELL, "3", "11000", "1", "32900")));

		Position position = state.positions().get(new PositionKey(ACCOUNT, ETF));
		assertThat(position.quantity()).isZero();
		assertThat(position.costKrw()).isZero();
		assertThat(position.realizedKrw()).isEqualByComparingTo("2885");
	}

	@Test
	void sellingMoreThanHeldIsRejected() {
		assertThatThrownBy(() -> HoldingsCalculator.replay(List.of(
				trade(BUY, "2", "10000", "1", "20000"),
				trade(SELL, "3", "10000", "1", "30000"))))
				.isInstanceOf(LedgerValidationException.class)
				.hasMessageContaining("보유 2, 매도 3");
	}

	@Test
	void foreignUnrealizedSplitsIntoPriceAndFxEffect() {
		LedgerState state = HoldingsCalculator.replay(List.of(
				trade(BUY, "10", "100", "1300", "1300000")));
		Position position = state.positions().get(new PositionKey(ACCOUNT, ETF));

		Valuation valuation = HoldingsCalculator.value(position, new BigDecimal("110"), new BigDecimal("1400"), true);

		assertThat(valuation.marketValueKrw()).isEqualByComparingTo("1540000");
		assertThat(valuation.unrealizedKrw()).isEqualByComparingTo("240000");
		assertThat(valuation.fxEffectKrw()).isEqualByComparingTo("100000");   // 1,000 USD x (1,400 - 1,300)
		assertThat(valuation.priceEffectKrw()).isEqualByComparingTo("140000"); // 10 x 10 USD x 1,400
		assertThat(valuation.returnRatePercent()).isEqualByComparingTo("18.46153846");
	}

	@Test
	void loanBalanceAndOverRepaymentGuard() {
		LedgerState state = HoldingsCalculator.replay(List.of(
				cash(LOAN_DRAW, "10000000"),
				cash(LOAN_REPAY, "1000000")));
		assertThat(state.liabilityByAccount().get(ACCOUNT)).isEqualByComparingTo("9000000");

		assertThatThrownBy(() -> HoldingsCalculator.replay(List.of(
				cash(LOAN_DRAW, "1000"),
				cash(LOAN_REPAY, "2000"))))
				.isInstanceOf(LedgerValidationException.class);
	}

	private static LedgerTransaction cash(TransactionType type, String amount) {
		return LedgerTransaction.builder().accountId(ACCOUNT).type(type).tradeDate(DAY)
				.amount(new BigDecimal(amount)).build();
	}

	private static LedgerTransaction dividend(String amount) {
		return LedgerTransaction.builder().accountId(ACCOUNT).type(DIVIDEND).tradeDate(DAY).instrumentId(ETF)
				.amount(new BigDecimal(amount)).build();
	}

	private static LedgerTransaction trade(TransactionType type, String quantity, String price, String fx, String amount) {
		return LedgerTransaction.builder().accountId(ACCOUNT).type(type).tradeDate(DAY).instrumentId(ETF)
				.quantity(new BigDecimal(quantity)).price(new BigDecimal(price)).fxRate(new BigDecimal(fx))
				.amount(new BigDecimal(amount)).build();
	}
}
