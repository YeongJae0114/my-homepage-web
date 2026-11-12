package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/** Regular sessions only. Exchange holidays and early closes are not inferred from collection time. */
public final class PortfolioMarketHours {

	private PortfolioMarketHours() {
	}

	public static ZoneId zone(Market market) {
		return ZoneId.of(market == Market.KRX ? "Asia/Seoul" : "America/New_York");
	}

	public static boolean collectionDue(Market market, ZonedDateTime now) {
		ZonedDateTime local = now.withZoneSameInstant(zone(market));
		LocalTime minute = local.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
		return weekday(local) && !minute.isBefore(open(market)) && !minute.isAfter(close(market).plusMinutes(10));
	}

	public static boolean afterClose(Market market, ZonedDateTime now) {
		return !now.withZoneSameInstant(zone(market)).toLocalTime().isBefore(close(market));
	}

	/** Allow the first 15 minutes after opening; closed sessions do not demand intraday refreshes. */
	public static boolean collectionDelayed(Market market, ZonedDateTime collectedAt, ZonedDateTime now) {
		ZonedDateTime local = now.withZoneSameInstant(zone(market));
		return weekday(local) && !local.toLocalTime().isBefore(open(market).plusMinutes(15))
				&& local.toLocalTime().isBefore(close(market))
				&& (collectedAt == null || Duration.between(collectedAt, now).compareTo(Duration.ofMinutes(15)) > 0);
	}

	private static boolean weekday(ZonedDateTime time) {
		return time.getDayOfWeek() != DayOfWeek.SATURDAY && time.getDayOfWeek() != DayOfWeek.SUNDAY;
	}

	private static LocalTime open(Market market) {
		return market == Market.KRX ? LocalTime.of(9, 0) : LocalTime.of(9, 30);
	}

	private static LocalTime close(Market market) {
		return market == Market.KRX ? LocalTime.of(15, 30) : LocalTime.of(16, 0);
	}
}
