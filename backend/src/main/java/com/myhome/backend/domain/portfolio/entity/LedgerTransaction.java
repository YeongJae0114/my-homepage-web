package com.myhome.backend.domain.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * One ledger entry. {@code amount} is the real settled KRW amount, always >= 0; the type decides its sign.
 * Account and instrument are plain FK ids: the ledger is replayed in bulk, never navigated.
 */
@Getter
@Entity
@Table(name = "portfolio_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerTransaction {

	public enum TransactionType {
		/** Starting balance: cash (no instrument), a position (instrument + qty + avg price), or a loan balance. */
		OPENING,
		DEPOSIT, WITHDRAW,
		/** Between the owner's own accounts; always created in pairs sharing transferGroupId. */
		TRANSFER_IN, TRANSFER_OUT,
		BUY, SELL,
		/** ETF distribution or stock dividend; amount is the net deposit, tax the withholding. */
		DIVIDEND,
		INTEREST, FEE, TAX,
		LOAN_DRAW, LOAN_REPAY
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long accountId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TransactionType type;

	@Column(nullable = false)
	private LocalDate tradeDate;

	private Long instrumentId;

	@Column(precision = 28, scale = 8)
	private BigDecimal quantity;

	@Column(precision = 19, scale = 4)
	private BigDecimal price;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal fxRate;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal fee;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal tax;

	private UUID transferGroupId;

	@Column(length = 500)
	private String memo;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private LedgerTransaction(
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
			String memo
	) {
		this.id = id;
		this.accountId = accountId;
		this.type = type;
		this.tradeDate = tradeDate;
		this.instrumentId = instrumentId;
		this.quantity = quantity;
		this.price = price;
		this.fxRate = fxRate == null ? BigDecimal.ONE : fxRate;
		this.amount = amount;
		this.fee = fee == null ? BigDecimal.ZERO : fee;
		this.tax = tax == null ? BigDecimal.ZERO : tax;
		this.transferGroupId = transferGroupId;
		this.memo = memo;
	}

	public void overwriteWith(LedgerTransaction source) {
		this.accountId = source.accountId;
		this.type = source.type;
		this.tradeDate = source.tradeDate;
		this.instrumentId = source.instrumentId;
		this.quantity = source.quantity;
		this.price = source.price;
		this.fxRate = source.fxRate;
		this.amount = source.amount;
		this.fee = source.fee;
		this.tax = source.tax;
		this.transferGroupId = source.transferGroupId;
		this.memo = source.memo;
	}
}
