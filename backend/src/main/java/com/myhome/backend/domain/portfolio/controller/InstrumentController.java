package com.myhome.backend.domain.portfolio.controller;

import com.myhome.backend.domain.portfolio.dto.InstrumentDtos.InstrumentRequest;
import com.myhome.backend.domain.portfolio.dto.InstrumentDtos.InstrumentResponse;
import com.myhome.backend.domain.portfolio.service.LedgerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Owner-only. Instruments are deactivated (active=false), never deleted. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio/instruments")
public class InstrumentController {

	private final LedgerService ledgerService;

	@GetMapping
	public List<InstrumentResponse> getInstruments() {
		return ledgerService.getInstruments();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public InstrumentResponse createInstrument(@Valid @RequestBody InstrumentRequest request) {
		return ledgerService.createInstrument(request);
	}

	@PutMapping("/{instrumentId}")
	public InstrumentResponse updateInstrument(
			@PathVariable Long instrumentId,
			@Valid @RequestBody InstrumentRequest request
	) {
		return ledgerService.updateInstrument(instrumentId, request);
	}
}
