package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.dto.AccountDtos.AccountRequest;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.AccountResponse;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.ValuationRequest;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.ValuationResponse;
import com.myhome.backend.domain.portfolio.dto.InstrumentDtos.InstrumentRequest;
import com.myhome.backend.domain.portfolio.dto.InstrumentDtos.InstrumentResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.CsvFile;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.CsvRequest;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.ImportResult;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.HoldingsResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionPage;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionRequest;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransferRequest;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType;
import java.time.LocalDate;
import java.util.List;

public interface LedgerService {

	List<AccountResponse> getAccounts();

	AccountResponse createAccount(AccountRequest request);

	AccountResponse updateAccount(Long accountId, AccountRequest request);

	List<ValuationResponse> getValuations(Long accountId);

	ValuationResponse saveValuation(Long accountId, ValuationRequest request);

	List<InstrumentResponse> getInstruments();

	InstrumentResponse createInstrument(InstrumentRequest request);

	InstrumentResponse updateInstrument(Long instrumentId, InstrumentRequest request);

	TransactionPage getTransactions(Long accountId, TransactionType type, LocalDate from, LocalDate to, int page, int size);

	CsvFile getTransactionCsvTemplate();

	CsvFile exportTransactionsCsv();

	ImportResult previewTransactionsCsv(CsvRequest request);

	ImportResult importTransactionsCsv(CsvRequest request);

	TransactionResponse createTransaction(TransactionRequest request);

	TransactionResponse updateTransaction(Long transactionId, TransactionRequest request);

	void deleteTransaction(Long transactionId);

	List<TransactionResponse> createTransfer(TransferRequest request);

	HoldingsResponse getHoldings();
}
