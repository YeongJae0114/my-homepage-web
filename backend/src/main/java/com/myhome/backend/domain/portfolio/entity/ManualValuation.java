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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Value that cannot be derived from the ledger (e.g. insurance surrender value). Not a cash flow. */
@Getter
@Entity
@Table(name = "portfolio_manual_valuations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManualValuation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long accountId;

	@Column(nullable = false)
	private LocalDate valuationDate;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(length = 500)
	private String memo;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public ManualValuation(Long accountId, LocalDate valuationDate, BigDecimal amount, String memo) {
		this.accountId = accountId;
		this.valuationDate = valuationDate;
		this.amount = amount;
		this.memo = memo;
	}

	public void update(BigDecimal amount, String memo) {
		this.amount = amount;
		this.memo = memo;
	}
}
