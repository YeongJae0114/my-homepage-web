package com.myhome.backend.domain.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class PortfolioMarketHoursTest {

	@Test
	void usSessionFollowsDaylightSavingAndNewYorkWeekdays() {
		assertThat(due(Market.NASDAQ, "2026-07-06T22:29:00+09:00[Asia/Seoul]")).isFalse();
		assertThat(due(Market.NASDAQ, "2026-07-06T22:30:00+09:00[Asia/Seoul]")).isTrue();
		assertThat(due(Market.NYSE, "2026-11-12T23:29:00+09:00[Asia/Seoul]")).isFalse();
		assertThat(due(Market.NYSE, "2026-11-12T23:30:00+09:00[Asia/Seoul]")).isTrue();
		// Saturday in Seoul is still Friday's session in New York.
		assertThat(due(Market.AMEX, "2026-11-14T05:00:00+09:00[Asia/Seoul]")).isTrue();
		assertThat(due(Market.AMEX, "2026-11-15T05:00:00+09:00[Asia/Seoul]")).isFalse();
		assertThat(due(Market.NASDAQ, "2026-11-13T06:10:01+09:00[Asia/Seoul]")).isTrue();
		assertThat(due(Market.NASDAQ, "2026-11-13T06:11:00+09:00[Asia/Seoul]")).isFalse();
	}

	@Test
	void domesticSessionAndStalenessHaveOpeningGraceAndNoNighttimeAlarm() {
		ZonedDateTime open = ZonedDateTime.parse("2026-11-12T09:00:00+09:00[Asia/Seoul]");
		assertThat(PortfolioMarketHours.collectionDue(Market.KRX, open.minusMinutes(1))).isFalse();
		assertThat(PortfolioMarketHours.collectionDue(Market.KRX, open)).isTrue();
		assertThat(PortfolioMarketHours.collectionDue(Market.KRX, open.withHour(15).withMinute(40))).isTrue();
		assertThat(PortfolioMarketHours.collectionDue(Market.KRX, open.withHour(15).withMinute(41))).isFalse();
		assertThat(PortfolioMarketHours.collectionDelayed(Market.KRX, open.minusDays(1), open.plusMinutes(14))).isFalse();
		assertThat(PortfolioMarketHours.collectionDelayed(Market.KRX, open.minusDays(1), open.plusMinutes(15))).isTrue();
		assertThat(PortfolioMarketHours.collectionDelayed(Market.KRX, open, open.plusMinutes(15))).isFalse();
		assertThat(PortfolioMarketHours.collectionDelayed(Market.KRX, open, open.plusMinutes(16))).isTrue();
		assertThat(PortfolioMarketHours.collectionDelayed(Market.KRX, open, open.withHour(20))).isFalse();
	}

	private boolean due(Market market, String instant) {
		return PortfolioMarketHours.collectionDue(market, ZonedDateTime.parse(instant));
	}
}
