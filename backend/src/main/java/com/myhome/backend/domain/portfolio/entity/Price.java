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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Close price in the instrument's trading currency. Intraday rows are updated in place until finalized. */
@Getter
@Entity
@Table(name = "portfolio_prices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Price {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long instrumentId;

	@Column(nullable = false)
	private LocalDate priceDate;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal closePrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private MarketDataSource source;

	@Column(name = "is_final", nullable = false)
	private boolean finalized;

	@Column(nullable = false)
	private LocalDateTime capturedAt;

	public Price(Long instrumentId, LocalDate priceDate) {
		this.instrumentId = instrumentId;
		this.priceDate = priceDate;
	}

	public void record(BigDecimal closePrice, MarketDataSource source, boolean finalized, LocalDateTime capturedAt) {
		this.closePrice = closePrice;
		this.source = source;
		this.finalized = finalized;
		this.capturedAt = capturedAt;
	}
}
