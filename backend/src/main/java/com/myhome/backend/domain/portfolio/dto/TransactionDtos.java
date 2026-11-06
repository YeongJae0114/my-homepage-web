package com.myhome.backend.domain.portfolio.dto;

import com.myhome.backend.domain.portfolio.entity.LedgerTransaction;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class TransactionDtos {

	private TransactionDtos() {
	}

	/** amount may be omitted for BUY / SELL / OPENING(position): it is then computed from qty x price x fx +/- fee, tax. */
	public record TransactionRequest(
			@NotNull Long accountId,
			@NotNull TransactionType type,
			@NotNull LocalDate tradeDate,
			Long instrumentId,
			@Positive BigDecimal quantity,
			@Positive BigDecimal price,
			@Positive BigDecimal fxRate,
			@PositiveOrZero BigDecimal amount,
			@PositiveOrZero BigDecimal fee,
			@PositiveOrZero BigDecimal tax,
			@Size(max = 500) String memo
	) {
	}

	/** Moves cash between two of the owner's accounts; stored as a TRANSFER_OUT / TRANSFER_IN pair. */
	public record TransferRequest(
			@NotNull Long fromAccountId,
			@NotNull Long toAccountId,
			@NotNull LocalDate tradeDate,
			@NotNull @Positive BigDecimal amount,
			@Size(max = 500) String memo
	) {
	}

	public record TransactionResponse(
			Long id,
			Long accountId,
			TransactionType type,
			LocalDate tradeDate,
			Long instrumentId,
			BigDecimal quantity,
			BigDecimal price,
			BigDecimal fxRate,
			BigDecimal amount,
			BigDecimal fee,
			BigDecimal tax,
			UUID transferGroupId,
			String memo,
			boolean editable
	) {
		/** editable: created after the latest snapshot (or no snapshot yet), so it can still be corrected. */
		public static TransactionResponse from(LedgerTransaction transaction, LocalDateTime lockedThrough) {
			return new TransactionResponse(
					transaction.getId(),
					transaction.getAccountId(),
					transaction.getType(),
					transaction.getTradeDate(),
					transaction.getInstrumentId(),
					PortfolioNumbers.plain(transaction.getQuantity()),
					PortfolioNumbers.plain(transaction.getPrice()),
					PortfolioNumbers.plain(transaction.getFxRate()),
					PortfolioNumbers.krw(transaction.getAmount()),
					PortfolioNumbers.krw(transaction.getFee()),
					PortfolioNumbers.krw(transaction.getTax()),
					transaction.getTransferGroupId(),
					transaction.getMemo(),
					lockedThrough == null || transaction.getCreatedAt() == null
							|| transaction.getCreatedAt().isAfter(lockedThrough));
		}
	}

	public record TransactionPage(List<TransactionResponse> items, int page, int size, long totalElements) {
	}

	/** Cost-basis view of the ledger. Market value arrives with prices in step 3. */
	public record HoldingsResponse(
			List<PositionResponse> positions,
			List<AccountBalanceResponse> balances,
			BigDecimal realizedProfit,
			BigDecimal dividendIncome
	) {
	}

	public record PositionResponse(
			Long accountId,
			Long instrumentId,
			BigDecimal quantity,
			BigDecimal avgPriceLocal,
			BigDecimal costKrw,
			BigDecimal realizedKrw,
			BigDecimal dividendsKrw
	) {
	}

	/** cashKrw for regular accounts, liabilityKrw for loan accounts. */
	public record AccountBalanceResponse(Long accountId, BigDecimal cashKrw, BigDecimal liabilityKrw) {
	}
}
