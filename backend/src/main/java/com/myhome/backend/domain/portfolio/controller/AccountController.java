package com.myhome.backend.domain.portfolio.controller;

import com.myhome.backend.domain.portfolio.dto.AccountDtos.AccountRequest;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.AccountResponse;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.ValuationRequest;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.ValuationResponse;
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

/** Owner-only (secured by /api/portfolio/** in SecurityConfig). Accounts are closed via closedAt, never deleted. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio/accounts")
public class AccountController {

	private final LedgerService ledgerService;

	@GetMapping
	public List<AccountResponse> getAccounts() {
		return ledgerService.getAccounts();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AccountResponse createAccount(@Valid @RequestBody AccountRequest request) {
		return ledgerService.createAccount(request);
	}

	@PutMapping("/{accountId}")
	public AccountResponse updateAccount(@PathVariable Long accountId, @Valid @RequestBody AccountRequest request) {
		return ledgerService.updateAccount(accountId, request);
	}

	@GetMapping("/{accountId}/valuations")
	public List<ValuationResponse> getValuations(@PathVariable Long accountId) {
		return ledgerService.getValuations(accountId);
	}

	@PostMapping("/{accountId}/valuations")
	public ValuationResponse saveValuation(@PathVariable Long accountId, @Valid @RequestBody ValuationRequest request) {
		return ledgerService.saveValuation(accountId, request);
	}
}
