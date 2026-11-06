package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.FxInputRequest;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.IntegrationsResponse;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.PriceInputRequest;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.SnapshotResponse;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface MarketDataService {

	void savePrices(PriceInputRequest request);

	void saveFxRates(FxInputRequest request);

	SnapshotResponse captureSnapshot();

	IntegrationsResponse getIntegrations();

	/** Held instruments that collectors should price: KIS-sourced, active, quantity > 0. */
	List<Instrument> collectionTargets(boolean domestic);

	Set<String> heldForeignCurrencies();

	/** Returns false when a MANUAL row already exists for that day (the owner's value wins). */
	boolean recordCollectedPrice(Long instrumentId, LocalDate date, BigDecimal closePrice, boolean finalized);

	boolean recordCollectedFx(String currency, LocalDate date, BigDecimal rateToKrw);
}
