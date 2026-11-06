package com.myhome.backend.domain.portfolio.controller;

import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.FxInputRequest;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.IntegrationsResponse;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.PriceInputRequest;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.SnapshotResponse;
import com.myhome.backend.domain.portfolio.service.MarketDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Owner-only: manual prices / FX (also used to correct collected values), snapshots, integration status. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
public class MarketDataController {

	private final MarketDataService marketDataService;

	@PutMapping("/prices")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void savePrices(@Valid @RequestBody PriceInputRequest request) {
		marketDataService.savePrices(request);
	}

	@PutMapping("/fx-rates")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void saveFxRates(@Valid @RequestBody FxInputRequest request) {
		marketDataService.saveFxRates(request);
	}

	@PostMapping("/snapshots")
	@ResponseStatus(HttpStatus.CREATED)
	public SnapshotResponse captureSnapshot() {
		return marketDataService.captureSnapshot();
	}

	@GetMapping("/integrations")
	public IntegrationsResponse getIntegrations() {
		return marketDataService.getIntegrations();
	}
}
