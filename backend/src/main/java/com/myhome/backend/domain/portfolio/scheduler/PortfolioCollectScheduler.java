package com.myhome.backend.domain.portfolio.scheduler;

import com.myhome.backend.domain.portfolio.client.IntegrationStatus;
import com.myhome.backend.domain.portfolio.client.KeximFxClient;
import com.myhome.backend.domain.portfolio.client.KisClient;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.service.MarketDataService;
import com.myhome.backend.domain.portfolio.service.SnapshotService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Market-data collection and the daily snapshot. Without API keys every collector logs once and skips;
 * registering the keys and redeploying is enough to start collection (no code change).
 * ponytail: no exchange holiday calendar. On a holiday KIS returns the previous close, stored under the
 * holiday's date (a flat day); the snapshot job skips days where nothing changed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioCollectScheduler {

	private static final String SEOUL = "Asia/Seoul";
	private static final ZoneId SEOUL_ZONE = ZoneId.of(SEOUL);
	private static final ZoneId NEW_YORK_ZONE = ZoneId.of("America/New_York");
	private static final LocalTime KRX_CLOSE = LocalTime.of(15, 30);
	private static final long CALL_INTERVAL_MILLIS = 100; // stays well under KIS per-second limits

	private final KisClient kisClient;
	private final KeximFxClient keximFxClient;
	private final MarketDataService marketDataService;
	private final SnapshotService snapshotService;
	private final IntegrationStatus integrationStatus;
	private final Set<String> warnedUnconfigured = ConcurrentHashMap.newKeySet();

	/** KRX session every 15 minutes; runs from 15:30 on finalize the day's close. */
	@Scheduled(cron = "${portfolio.schedule.kr-prices:0 */15 9-15 * * MON-FRI}", zone = SEOUL)
	public void collectDomesticPrices() {
		if (!ready(IntegrationStatus.KIS, kisClient.isConfigured())) {
			return;
		}
		LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);
		collectPrices(true, now.toLocalDate(), !now.toLocalTime().isBefore(KRX_CLOSE));
	}

	/** 07:00 KST Tue-Sat = after the US close of Mon-Fri; stored under the US trading date. */
	@Scheduled(cron = "${portfolio.schedule.us-prices:0 0 7 * * TUE-SAT}", zone = SEOUL)
	public void collectUsPrices() {
		if (!ready(IntegrationStatus.KIS, kisClient.isConfigured())) {
			return;
		}
		collectPrices(false, LocalDate.now(NEW_YORK_ZONE), true);
	}

	/** Eximbank publishes around 11:00 KST on business days. */
	@Scheduled(cron = "${portfolio.schedule.fx:0 30 11 * * MON-FRI}", zone = SEOUL)
	public void collectFxRates() {
		if (!ready(IntegrationStatus.KEXIM, keximFxClient.isConfigured())) {
			return;
		}
		Set<String> currencies = marketDataService.heldForeignCurrencies();
		if (currencies.isEmpty()) {
			integrationStatus.record(IntegrationStatus.KEXIM, true, "보유 외화 종목 없음");
			return;
		}

		LocalDate today = LocalDate.now(SEOUL_ZONE);
		try {
			Map<String, BigDecimal> rates = keximFxClient.fetchRates(today);
			if (rates.isEmpty()) {
				integrationStatus.record(IntegrationStatus.KEXIM, true, "고시 환율 없음 (휴일 또는 고시 전)");
				return;
			}
			int saved = 0;
			for (String currency : currencies) {
				BigDecimal rate = rates.get(currency);
				if (rate != null && marketDataService.recordCollectedFx(currency, today, rate)) {
					saved++;
				}
			}
			String result = "환율 %s: %d/%d 저장".formatted(today, saved, currencies.size());
			integrationStatus.record(IntegrationStatus.KEXIM, true, result);
			log.info("KEXIM collection: {}", result);
		} catch (RuntimeException exception) {
			integrationStatus.record(IntegrationStatus.KEXIM, true, "실패: " + exception.getMessage());
			log.warn("KEXIM collection failed: {}", exception.getMessage());
		}
	}

	/**
	 * 18:00 KST: after KRX after-hours trading ends (the official 15:30 close is already final). Capturing freezes
	 * the day: entries made before it become read-only, so later is friendlier for same-day corrections.
	 */
	@Scheduled(cron = "${portfolio.schedule.snapshot:0 0 18 * * MON-FRI}", zone = SEOUL)
	public void captureSnapshot() {
		boolean captured = snapshotService.captureIfChanged(LocalDate.now(SEOUL_ZONE));
		log.info("Portfolio snapshot {}.", captured ? "captured" : "skipped (no changes since the last one)");
	}

	private void collectPrices(boolean domestic, LocalDate priceDate, boolean finalized) {
		int saved = 0;
		int failed = 0;
		int manualKept = 0;
		for (Instrument instrument : marketDataService.collectionTargets(domestic)) {
			try {
				BigDecimal price = domestic
						? kisClient.fetchDomesticPrice(instrument.getSymbol())
						: kisClient.fetchOverseasPrice(instrument.getMarket(), instrument.getSymbol());
				if (marketDataService.recordCollectedPrice(instrument.getId(), priceDate, price, finalized)) {
					saved++;
				} else {
					manualKept++;
				}
			} catch (RuntimeException exception) {
				// One failing symbol must not stop the rest; its last price stays and shows as stale.
				failed++;
				log.warn("KIS price failed for {}: {}", instrument.getSymbol(), exception.getMessage());
			}
			pause();
		}
		String result = "%s %s: 저장 %d, 실패 %d, 수동값 유지 %d"
				.formatted(domestic ? "국내" : "해외", priceDate, saved, failed, manualKept);
		integrationStatus.record(IntegrationStatus.KIS, true, result);
		log.info("KIS collection: {}", result);
	}

	private boolean ready(String source, boolean configured) {
		if (configured) {
			return true;
		}
		if (warnedUnconfigured.add(source)) {
			log.info("{} API key is not configured; collection skipped (manual input still works).", source);
		}
		integrationStatus.record(source, false, "키 미설정: 수집 건너뜀");
		return false;
	}

	private static void pause() {
		try {
			Thread.sleep(CALL_INTERVAL_MILLIS);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}
}
