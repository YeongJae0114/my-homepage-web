package com.myhome.backend.domain.portfolio.dto;

import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.Instrument.AssetClass;
import com.myhome.backend.domain.portfolio.entity.Instrument.InstrumentType;
import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import com.myhome.backend.domain.portfolio.entity.Instrument.PriceSource;
import com.myhome.backend.domain.portfolio.entity.Instrument.Region;
import com.myhome.backend.domain.portfolio.entity.Instrument.TaxType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public final class InstrumentDtos {

	private InstrumentDtos() {
	}

	public record InstrumentRequest(
			@NotBlank @Size(max = 20) String symbol,
			@NotBlank @Size(max = 100) String name,
			@NotNull Market market,
			@NotNull InstrumentType instrumentType,
			@NotNull AssetClass underlyingAssetClass,
			@NotNull Region exposureRegion,
			boolean fxHedged,
			@NotNull TaxType taxType,
			PriceSource priceSource,
			Boolean active
	) {
		/** Trading currency follows the market: KRX trades in KRW, US exchanges in USD. */
		public Instrument toEntity() {
			return Instrument.builder()
					.symbol(symbol.trim().toUpperCase(Locale.ROOT))
					.name(name.trim())
					.market(market)
					.tradingCurrency(market == Market.KRX ? "KRW" : "USD")
					.instrumentType(instrumentType)
					.underlyingAssetClass(underlyingAssetClass)
					.exposureRegion(exposureRegion)
					.fxHedged(fxHedged)
					.taxType(taxType)
					.priceSource(priceSource == null ? PriceSource.KIS : priceSource)
					.active(active == null || active)
					.build();
		}
	}

	public record InstrumentResponse(
			Long id,
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
		public static InstrumentResponse from(Instrument instrument) {
			return new InstrumentResponse(
					instrument.getId(),
					instrument.getSymbol(),
					instrument.getName(),
					instrument.getMarket(),
					instrument.getTradingCurrency(),
					instrument.getInstrumentType(),
					instrument.getUnderlyingAssetClass(),
					instrument.getExposureRegion(),
					instrument.isFxHedged(),
					instrument.getTaxType(),
					instrument.getPriceSource(),
					instrument.isActive());
		}
	}
}
