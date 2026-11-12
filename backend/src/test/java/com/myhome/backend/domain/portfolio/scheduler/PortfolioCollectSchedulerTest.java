package com.myhome.backend.domain.portfolio.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.myhome.backend.domain.portfolio.client.IntegrationStatus;
import com.myhome.backend.domain.portfolio.client.KeximFxClient;
import com.myhome.backend.domain.portfolio.client.KisClient;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import com.myhome.backend.domain.portfolio.service.MarketDataService;
import com.myhome.backend.domain.portfolio.service.SnapshotService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class PortfolioCollectSchedulerTest {
	private final KisClient kis = mock(KisClient.class);
	private final MarketDataService data = mock(MarketDataService.class);
	private final PortfolioCollectScheduler scheduler = new PortfolioCollectScheduler(
			kis, mock(KeximFxClient.class), data, mock(SnapshotService.class), new IntegrationStatus());

	@Test
	void intradayUsesUsDateAndOneFailedSymbolDoesNotStopTheOthers() {
		Instrument failed = mock(Instrument.class);
		Instrument success = mock(Instrument.class);
		when(failed.getSymbol()).thenReturn("FAIL");
		when(failed.getMarket()).thenReturn(Market.NASDAQ);
		when(success.getId()).thenReturn(2L);
		when(success.getSymbol()).thenReturn("TEST");
		when(success.getMarket()).thenReturn(Market.NYSE);
		when(data.collectionTargets(false)).thenReturn(List.of(failed, success));
		when(kis.fetchOverseasPrice(Market.NASDAQ, "FAIL")).thenThrow(new IllegalStateException("fixture failure"));
		when(kis.fetchOverseasPrice(Market.NYSE, "TEST")).thenReturn(new BigDecimal("123.45"));

		scheduler.collectSession(Market.NASDAQ, ZonedDateTime.parse("2026-11-13T00:05:00+09:00[Asia/Seoul]"));
		verify(data).recordCollectedPrice(2L, LocalDate.of(2026, 11, 12), new BigDecimal("123.45"), false);
		scheduler.collectSession(Market.NASDAQ, ZonedDateTime.parse("2026-11-13T06:05:00+09:00[Asia/Seoul]"));
		verify(data).recordCollectedPrice(eq(2L), eq(LocalDate.of(2026, 11, 12)), any(), eq(true));
	}

	@Test
	void weekendsAndMissingKeysDoNotFetchQuotes() {
		scheduler.collectSession(Market.KRX, ZonedDateTime.parse("2026-11-14T10:00:00+09:00[Asia/Seoul]"));
		scheduler.collectUsIntradayPrices();
		verifyNoInteractions(data);
		verify(kis, never()).fetchOverseasPrice(any(), any());
	}
}
