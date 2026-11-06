package com.myhome.backend.domain.portfolio.dto;

import com.myhome.backend.domain.portfolio.entity.PortfolioSnapshot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public final class MarketDataDtos {

	private MarketDataDtos() {
	}

	/** Manual close prices for one date (trading currency). Manual rows win over collected ones. */
	public record PriceInputRequest(@NotNull LocalDate date, @NotEmpty List<@Valid PriceItem> items) {
	}

	public record PriceItem(@NotNull Long instrumentId, @NotNull @Positive BigDecimal closePrice) {
	}

	/** KRW per one unit of the currency (enter JPY per 1 yen, not per 100). */
	public record FxInputRequest(@NotNull LocalDate date, @NotEmpty List<@Valid FxItem> items) {
	}

	public record FxItem(
			@NotNull @Pattern(regexp = "[A-Z]{3}", message = "통화 코드는 USD처럼 대문자 3자리입니다") String currency,
			@NotNull @Positive BigDecimal rateToKrw
	) {
	}

	public record SnapshotResponse(
			LocalDate snapshotDate,
			BigDecimal totalAssets,
			BigDecimal totalLiabilities,
			BigDecimal netWorth,
			LocalDate priceAsOf
	) {
		public static SnapshotResponse from(PortfolioSnapshot snapshot) {
			return new SnapshotResponse(
					snapshot.getSnapshotDate(),
					PortfolioNumbers.krw(snapshot.getTotalAssets()),
					PortfolioNumbers.krw(snapshot.getTotalLiabilities()),
					PortfolioNumbers.krw(snapshot.getNetWorth()),
					snapshot.getPriceAsOf());
		}
	}

	/** Never contains keys: only whether a source is configured and how its last run went. */
	public record IntegrationsResponse(SourceStatus kis, SourceStatus kexim) {
	}

	public record SourceStatus(boolean configured, OffsetDateTime lastRunAt, String lastResult) {
	}
}
