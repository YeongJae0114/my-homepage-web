package com.myhome.backend.domain.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Daily valuation, frozen once captured (only today's may be recaptured). Ledger entries made before the
 * latest capture are locked, so the books and the frozen history stay consistent.
 * scopeValue / scopeNetFlow cover the investment accounts and feed TWR.
 */
@Getter
@Entity
@Table(name = "portfolio_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private LocalDate snapshotDate;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal cashEquivalent;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal investment;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal pensionInsurance;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal totalAssets;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal totalLiabilities;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal netWorth;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal scopeValue;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal scopeNetFlow;

	/** Scope contributions dated on or before the snapshot date, as known when it was captured. */
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal scopeCumulativeFlow;

	private LocalDate priceAsOf;

	@Column(nullable = false)
	private LocalDateTime computedAt;

	public PortfolioSnapshot(LocalDate snapshotDate) {
		this.snapshotDate = snapshotDate;
	}

	public void record(
			BigDecimal cashEquivalent,
			BigDecimal investment,
			BigDecimal pensionInsurance,
			BigDecimal totalLiabilities,
			BigDecimal scopeValue,
			BigDecimal scopeNetFlow,
			BigDecimal scopeCumulativeFlow,
			LocalDate priceAsOf,
			LocalDateTime computedAt
	) {
		this.scopeCumulativeFlow = scopeCumulativeFlow;
		this.cashEquivalent = cashEquivalent;
		this.investment = investment;
		this.pensionInsurance = pensionInsurance;
		this.totalAssets = cashEquivalent.add(investment).add(pensionInsurance);
		this.totalLiabilities = totalLiabilities;
		this.netWorth = totalAssets.subtract(totalLiabilities);
		this.scopeValue = scopeValue;
		this.scopeNetFlow = scopeNetFlow;
		this.priceAsOf = priceAsOf;
		this.computedAt = computedAt;
	}
}
