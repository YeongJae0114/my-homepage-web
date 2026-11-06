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

/** KRW per ONE unit of the currency (JPY is normalized from the per-100 quote before saving). */
@Getter
@Entity
@Table(name = "portfolio_fx_rates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FxRate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(nullable = false)
	private LocalDate rateDate;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal rateToKrw;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private MarketDataSource source;

	@Column(nullable = false)
	private LocalDateTime capturedAt;

	public FxRate(String currency, LocalDate rateDate) {
		this.currency = currency;
		this.rateDate = rateDate;
	}

	public void record(BigDecimal rateToKrw, MarketDataSource source, LocalDateTime capturedAt) {
		this.rateToKrw = rateToKrw;
		this.source = source;
		this.capturedAt = capturedAt;
	}
}
