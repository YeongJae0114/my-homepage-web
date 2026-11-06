package com.myhome.backend.domain.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Security master. Listing (market, trading currency) is kept apart from real exposure
 * (region, asset class, hedging): e.g. a KRX-listed S&P500 ETF trades in KRW but is US equity.
 */
@Getter
@Entity
@Table(name = "portfolio_instruments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Instrument {

	public enum Market { KRX, NASDAQ, NYSE, AMEX }

	public enum InstrumentType { ETF, STOCK }

	public enum AssetClass { EQUITY, BOND, COMMODITY, REIT, CASH_LIKE, MIXED }

	public enum Region { KR, US, GLOBAL, DM, EM, CN, JP, OTHER }

	public enum TaxType { DOMESTIC_EQUITY_ETF, OTHER_ETF, DOMESTIC_STOCK, FOREIGN_STOCK }

	public enum PriceSource { KIS, MANUAL }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 20)
	private String symbol;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Market market;

	@Column(nullable = false, length = 3)
	private String tradingCurrency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private InstrumentType instrumentType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AssetClass underlyingAssetClass;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Region exposureRegion;

	@Column(nullable = false)
	private boolean fxHedged;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TaxType taxType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private PriceSource priceSource;

	@Column(nullable = false)
	private boolean active;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private Instrument(
			String symbol,
			String name,
			Market market,
			String tradingCurrency,
			InstrumentType instrumentType,
			AssetClass underlyingAssetClass,
			Region exposureRegion,
			boolean fxHedged,
			TaxType taxType,
			PriceSource priceSource,
			boolean active
	) {
		this.symbol = symbol;
		this.name = name;
		this.market = market;
		this.tradingCurrency = tradingCurrency;
		this.instrumentType = instrumentType;
		this.underlyingAssetClass = underlyingAssetClass;
		this.exposureRegion = exposureRegion;
		this.fxHedged = fxHedged;
		this.taxType = taxType;
		this.priceSource = priceSource;
		this.active = active;
	}

	public boolean isForeignCurrency() {
		return !"KRW".equals(tradingCurrency);
	}

	public void overwriteWith(Instrument source) {
		this.symbol = source.symbol;
		this.name = source.name;
		this.market = source.market;
		this.tradingCurrency = source.tradingCurrency;
		this.instrumentType = source.instrumentType;
		this.underlyingAssetClass = source.underlyingAssetClass;
		this.exposureRegion = source.exposureRegion;
		this.fxHedged = source.fxHedged;
		this.taxType = source.taxType;
		this.priceSource = source.priceSource;
		this.active = source.active;
	}
}
