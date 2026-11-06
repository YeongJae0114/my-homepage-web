package com.myhome.backend.domain.portfolio.controller;

import com.myhome.backend.domain.portfolio.dto.TransactionDtos.HoldingsResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionPage;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionRequest;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransferRequest;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType;
import com.myhome.backend.domain.portfolio.service.LedgerService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Owner-only ledger API. Every write replays the affected account and is rejected if the books break. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
public class TransactionController {

	private final LedgerService ledgerService;

	@GetMapping("/transactions")
	public TransactionPage getTransactions(
			@RequestParam(required = false) Long accountId,
			@RequestParam(required = false) TransactionType type,
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size
	) {
		return ledgerService.getTransactions(accountId, type, from, to, page, size);
	}

	@PostMapping("/transactions")
	@ResponseStatus(HttpStatus.CREATED)
	public TransactionResponse createTransaction(@Valid @RequestBody TransactionRequest request) {
		return ledgerService.createTransaction(request);
	}

	@PutMapping("/transactions/{transactionId}")
	public TransactionResponse updateTransaction(
			@PathVariable Long transactionId,
			@Valid @RequestBody TransactionRequest request
	) {
		return ledgerService.updateTransaction(transactionId, request);
	}

	@DeleteMapping("/transactions/{transactionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTransaction(@PathVariable Long transactionId) {
		ledgerService.deleteTransaction(transactionId);
	}

	@PostMapping("/transfers")
	@ResponseStatus(HttpStatus.CREATED)
	public List<TransactionResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
		return ledgerService.createTransfer(request);
	}

	@GetMapping("/holdings")
	public HoldingsResponse getHoldings() {
		return ledgerService.getHoldings();
	}
}
