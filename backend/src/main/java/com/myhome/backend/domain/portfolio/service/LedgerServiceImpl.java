package com.myhome.backend.domain.portfolio.service;

import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.BUY;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.DIVIDEND;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.LOAN_DRAW;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.LOAN_REPAY;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.OPENING;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.SELL;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.TRANSFER_IN;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.TRANSFER_OUT;

import com.myhome.backend.domain.portfolio.dto.AccountDtos.AccountRequest;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.AccountResponse;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.ValuationRequest;
import com.myhome.backend.domain.portfolio.dto.AccountDtos.ValuationResponse;
import com.myhome.backend.domain.portfolio.dto.InstrumentDtos.InstrumentRequest;
import com.myhome.backend.domain.portfolio.dto.InstrumentDtos.InstrumentResponse;
import com.myhome.backend.domain.portfolio.dto.PortfolioNumbers;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.OpeningBasis;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.CsvFile;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.CsvRequest;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.ImportResult;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.ImportRow;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.AccountBalanceResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.HoldingsResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.PositionResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionPage;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionRequest;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionResponse;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransferRequest;
import com.myhome.backend.domain.portfolio.entity.Account;
import com.myhome.backend.domain.portfolio.entity.Account.AccountType;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType;
import com.myhome.backend.domain.portfolio.entity.ManualValuation;
import com.myhome.backend.domain.portfolio.repository.AccountRepository;
import com.myhome.backend.domain.portfolio.repository.InstrumentRepository;
import com.myhome.backend.domain.portfolio.repository.LedgerTransactionRepository;
import com.myhome.backend.domain.portfolio.repository.ManualValuationRepository;
import com.myhome.backend.domain.portfolio.service.HoldingsCalculator.LedgerState;
import com.myhome.backend.domain.portfolio.service.TransactionCsv.Entry;
import com.myhome.backend.domain.portfolio.service.TransactionCsv.Row;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerServiceImpl implements LedgerService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
	private static final Pattern KRX_SYMBOL = Pattern.compile("[0-9A-Z]{6}");
	private static final Pattern US_SYMBOL = Pattern.compile("[A-Z][A-Z0-9.\\-]{0,9}");
	private static final Set<TransactionType> POSITION_TYPES = Set.of(BUY, SELL, DIVIDEND);
	private static final Set<TransactionType> TRADE_TYPES = Set.of(BUY, SELL);
	private static final int MAX_PAGE_SIZE = 200;
	private static final int CSV_PREVIEW_SIZE = 30;
	private static final DateTimeFormatter LOCK_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

	private final AccountRepository accountRepository;
	private final InstrumentRepository instrumentRepository;
	private final LedgerTransactionRepository transactionRepository;
	private final ManualValuationRepository valuationRepository;
	private final SnapshotService snapshotService;
	private final MarketDataService marketDataService;

	// --- accounts -------------------------------------------------------------------------------------

	@Override
	public List<AccountResponse> getAccounts() {
		return accountRepository.findAllByOrderByDisplayOrderAscIdAsc().stream().map(AccountResponse::from).toList();
	}

	@Override
	@Transactional
	public AccountResponse createAccount(AccountRequest request) {
		return AccountResponse.from(accountRepository.save(request.toEntity()));
	}

	@Override
	@Transactional
	public AccountResponse updateAccount(Long accountId, AccountRequest request) {
		Account account = findAccount(accountId);
		if (account.getAccountType() != request.accountType() && transactionRepository.existsByAccountId(accountId)) {
			throw new LedgerValidationException("거래가 있는 계좌는 유형을 바꿀 수 없습니다");
		}
		account.overwriteWith(request.toEntity());
		return AccountResponse.from(account);
	}

	@Override
	public List<ValuationResponse> getValuations(Long accountId) {
		findAccount(accountId);
		return valuationRepository.findByAccountIdOrderByValuationDateDesc(accountId).stream()
				.map(ValuationResponse::from)
				.toList();
	}

	/** Upsert by date: one valuation per account per day. */
	@Override
	@Transactional
	public ValuationResponse saveValuation(Long accountId, ValuationRequest request) {
		Account account = findAccount(accountId);
		if (account.getAccountType() != AccountType.INSURANCE) {
			throw new LedgerValidationException("수동 평가는 보험 계좌에만 입력할 수 있습니다");
		}
		rejectFutureDate(request.valuationDate());

		ManualValuation valuation = valuationRepository.findByAccountIdAndValuationDate(accountId, request.valuationDate())
				.orElseGet(() -> new ManualValuation(accountId, request.valuationDate(), request.amount(), request.memo()));
		if (valuation.getId() != null) {
			ensureEditable(valuation.getCreatedAt(), "평가금액은");
		}
		valuation.update(request.amount(), request.memo());
		return ValuationResponse.from(valuationRepository.save(valuation));
	}

	// --- instruments ----------------------------------------------------------------------------------

	@Override
	public List<InstrumentResponse> getInstruments() {
		return instrumentRepository.findAllByOrderByNameAsc().stream().map(InstrumentResponse::from).toList();
	}

	@Override
	@Transactional
	public InstrumentResponse createInstrument(InstrumentRequest request) {
		Instrument candidate = request.toEntity();
		validateInstrument(candidate, null);
		return InstrumentResponse.from(instrumentRepository.save(candidate));
	}

	@Override
	@Transactional
	public InstrumentResponse updateInstrument(Long instrumentId, InstrumentRequest request) {
		Instrument instrument = instrumentRepository.findById(instrumentId)
				.orElseThrow(() -> notFound("종목을 찾을 수 없습니다"));
		Instrument candidate = request.toEntity();
		validateInstrument(candidate, instrumentId);

		boolean identityChanged = instrument.getMarket() != candidate.getMarket()
				|| !instrument.getSymbol().equals(candidate.getSymbol());
		if (identityChanged && transactionRepository.existsByInstrumentId(instrumentId)) {
			throw new LedgerValidationException("거래가 있는 종목은 시장·종목코드를 바꿀 수 없습니다");
		}
		instrument.overwriteWith(candidate);
		return InstrumentResponse.from(instrument);
	}

	private void validateInstrument(Instrument candidate, Long selfId) {
		Pattern symbolPattern = candidate.getMarket() == Market.KRX ? KRX_SYMBOL : US_SYMBOL;
		if (!symbolPattern.matcher(candidate.getSymbol()).matches()) {
			throw new LedgerValidationException(candidate.getMarket() == Market.KRX
					? "KRX 종목코드는 6자리입니다 (예: 069500)"
					: "미국 종목은 티커를 입력하세요 (예: AAPL)");
		}
		instrumentRepository.findByMarketAndSymbol(candidate.getMarket(), candidate.getSymbol())
				.filter(existing -> !existing.getId().equals(selfId))
				.ifPresent(existing -> {
					throw new LedgerValidationException("이미 등록된 종목입니다 (#%d)".formatted(existing.getId()));
				});
	}

	// --- transactions ---------------------------------------------------------------------------------

	@Override
	public TransactionPage getTransactions(
			Long accountId, TransactionType type, LocalDate from, LocalDate to, int page, int size) {
		Specification<LedgerTransaction> spec = (root, query, cb) -> cb.conjunction();
		if (accountId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("accountId"), accountId));
		}
		if (type != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
		}
		if (from != null) {
			spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("tradeDate"), from));
		}
		if (to != null) {
			spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("tradeDate"), to));
		}

		PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, MAX_PAGE_SIZE)),
				Sort.by(Sort.Order.desc("tradeDate"), Sort.Order.desc("id")));
		Page<LedgerTransaction> result = transactionRepository.findAll(spec, pageRequest);
		LocalDateTime lockedThrough = snapshotService.lockedThrough().orElse(null);
		return new TransactionPage(
				result.getContent().stream().map(transaction -> TransactionResponse.from(transaction, lockedThrough)).toList(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements());
	}

	@Override
	public CsvFile getTransactionCsvTemplate() {
		return new CsvFile("portfolio-transactions-template.csv", TransactionCsv.write(List.of(), Map.of(), Map.of()));
	}

	@Override
	public CsvFile exportTransactionsCsv() {
		List<Entry> entries = TransactionCsv.entries(transactionRepository.findAllByOrderByTradeDateAscIdAsc());
		Map<Long, Account> accounts = accountRepository.findAll().stream()
				.collect(Collectors.toMap(Account::getId, Function.identity()));
		Map<Long, Instrument> instruments = instrumentRepository.findAll().stream()
				.collect(Collectors.toMap(Instrument::getId, Function.identity()));
		return new CsvFile("portfolio-transactions-" + LocalDate.now(ZONE) + ".csv",
				TransactionCsv.write(entries, accounts, instruments));
	}

	@Override
	public ImportResult previewTransactionsCsv(CsvRequest request) {
		return prepareCsv(TransactionCsv.read(request.content()), accountRepository.findAll()).result();
	}

	@Override
	@Transactional
	public ImportResult importTransactionsCsv(CsvRequest request) {
		List<Row> rows = TransactionCsv.read(request.content());
		CsvImport plan = prepareCsv(rows, accountRepository.lockAllForLedgerWrite());
		transactionRepository.saveAll(plan.additions());
		return plan.result();
	}

	private record CsvImport(ImportResult result, List<LedgerTransaction> additions) {
	}

	private CsvImport prepareCsv(List<Row> rows, List<Account> availableAccounts) {
		Map<Long, Account> accounts = availableAccounts.stream()
				.collect(Collectors.toMap(Account::getId, Function.identity()));
		List<Instrument> availableInstruments = instrumentRepository.findAll();
		Map<String, Instrument> instruments = availableInstruments.stream()
				.collect(Collectors.toMap(TransactionCsv::instrumentCode, Function.identity()));
		Map<Long, Instrument> instrumentsById = availableInstruments.stream()
				.collect(Collectors.toMap(Instrument::getId, Function.identity()));
		List<LedgerTransaction> existing = transactionRepository.findAllByOrderByTradeDateAscIdAsc();
		Map<List<String>, Integer> remainingMatches = new HashMap<>();
		TransactionCsv.entries(existing).forEach(entry -> remainingMatches.merge(entry.key(), 1, Integer::sum));

		List<ImportRow> preview = new ArrayList<>();
		List<LedgerTransaction> additions = new ArrayList<>();
		int duplicateRows = 0;
		for (Row row : rows) {
			Entry entry = csvEntry(row, accounts, instruments);
			List<String> key = entry.key();
			int matches = remainingMatches.getOrDefault(key, 0);
			boolean duplicate = matches > 0;
			if (duplicate) {
				remainingMatches.put(key, matches - 1);
				duplicateRows++;
			} else {
				additions.addAll(entry.legs());
			}
			if (preview.size() < CSV_PREVIEW_SIZE) {
				LedgerTransaction trade = entry.trade();
				Instrument instrument = instrumentsById.get(trade.getInstrumentId());
				String toAccount = entry.incoming() == null ? null : accounts.get(entry.incoming().getAccountId()).getName();
				preview.add(new ImportRow(row.line(), trade.getTradeDate(), accounts.get(trade.getAccountId()).getName(),
						entry.label(), instrument == null ? null : instrument.getName(),
						trade.getAmount().stripTrailingZeros().toPlainString(), toAccount, duplicate));
			}
		}
		verifyCsvAdditions(existing, additions, accounts);
		return new CsvImport(new ImportResult(rows.size() - duplicateRows, duplicateRows, preview), additions);
	}

	private Entry csvEntry(Row row, Map<Long, Account> accounts, Map<String, Instrument> instruments) {
		TransactionRequest transaction = row.transfer() ? null : row.transactionRequest(accounts, instruments);
		TransferRequest transfer = transaction == null ? row.transferRequest(accounts) : null;
		Entry entry;
		try {
			entry = transaction == null ? toTransfer(transfer) : new Entry(toTransaction(transaction), null);
		} catch (LedgerValidationException exception) {
			throw row.error(exception.getMessage());
		}
		if (entry.trade().getAmount().precision() > 17) {
			throw row.error("정산금액이 저장할 수 있는 범위를 초과합니다");
		}
		return entry;
	}

	private static void verifyCsvAdditions(List<LedgerTransaction> existing, List<LedgerTransaction> additions,
			Map<Long, Account> accounts) {
		Map<Long, List<LedgerTransaction>> books = new LinkedHashMap<>();
		additions.forEach(trade -> books.computeIfAbsent(trade.getAccountId(), accountId -> new ArrayList<>()));
		for (LedgerTransaction trade : existing) {
			List<LedgerTransaction> book = books.get(trade.getAccountId());
			if (book != null) {
				book.add(trade);
			}
		}
		additions.forEach(trade -> books.get(trade.getAccountId()).add(trade));
		books.forEach((accountId, book) -> {
			// Existing entries stay ahead of newly inserted rows on the same trade date.
			book.sort(Comparator.comparing(LedgerTransaction::getTradeDate));
			try {
				HoldingsCalculator.replay(book);
			} catch (LedgerValidationException exception) {
				throw new LedgerValidationException("%s 계좌의 CSV를 확인하세요. %s"
						.formatted(accounts.get(accountId).getName(), exception.getMessage()));
			}
		});
	}

	@Override
	@Transactional
	public TransactionResponse createTransaction(TransactionRequest request) {
		accountRepository.lockAllForLedgerWrite();
		LedgerTransaction saved = transactionRepository.save(toTransaction(request));
		verifyLedger(saved.getAccountId());
		return TransactionResponse.from(saved, null);
	}

	@Override
	@Transactional
	public TransactionResponse updateTransaction(Long transactionId, TransactionRequest request) {
		accountRepository.lockAllForLedgerWrite();
		LedgerTransaction transaction = findTransaction(transactionId);
		ensureEditable(transaction.getCreatedAt(), "거래는");
		if (transaction.getTransferGroupId() != null) {
			throw new LedgerValidationException("이체는 수정할 수 없습니다. 삭제 후 다시 등록하세요");
		}

		Long previousAccountId = transaction.getAccountId();
		transaction.overwriteWith(toTransaction(request));
		verifyLedger(previousAccountId);
		if (!previousAccountId.equals(transaction.getAccountId())) {
			verifyLedger(transaction.getAccountId());
		}
		return TransactionResponse.from(transaction, null);
	}

	/** Deleting either leg of a transfer deletes both. */
	@Override
	@Transactional
	public void deleteTransaction(Long transactionId) {
		accountRepository.lockAllForLedgerWrite();
		LedgerTransaction transaction = findTransaction(transactionId);
		List<LedgerTransaction> removed = transaction.getTransferGroupId() == null
				? List.of(transaction)
				: transactionRepository.findByTransferGroupId(transaction.getTransferGroupId());

		removed.forEach(leg -> ensureEditable(leg.getCreatedAt(), "거래는"));
		transactionRepository.deleteAll(removed);
		removed.stream().map(LedgerTransaction::getAccountId).distinct().forEach(this::verifyLedger);
	}

	@Override
	@Transactional
	public List<TransactionResponse> createTransfer(TransferRequest request) {
		accountRepository.lockAllForLedgerWrite();
		return transactionRepository.saveAll(toTransfer(request).legs()).stream()
				.map(leg -> TransactionResponse.from(leg, null)).toList();
	}

	private Entry toTransfer(TransferRequest request) {
		if (request.fromAccountId().equals(request.toAccountId())) {
			throw new LedgerValidationException("출금 계좌와 입금 계좌가 같습니다");
		}
		Account from = findAccount(request.fromAccountId());
		Account to = findAccount(request.toAccountId());
		if (from.getAccountType() == AccountType.LOAN || to.getAccountType() == AccountType.LOAN) {
			throw new LedgerValidationException("대출 계좌는 이체 대상이 아닙니다. LOAN_DRAW / LOAN_REPAY를 사용하세요");
		}
		rejectFutureDate(request.tradeDate());

		UUID groupId = UUID.randomUUID();
		BigDecimal amount = krw(request.amount());
		return new Entry(
				transferLeg(TRANSFER_OUT, from.getId(), request, amount, groupId),
				transferLeg(TRANSFER_IN, to.getId(), request, amount, groupId));
	}

	@Override
	public HoldingsResponse getHoldings() {
		LedgerState state = HoldingsCalculator.replay(transactionRepository.findAllByOrderByTradeDateAscIdAsc());

		List<PositionResponse> positions = state.positions().values().stream()
				.filter(position -> position.quantity().signum() > 0)
				.map(position -> new PositionResponse(
						position.accountId(),
						position.instrumentId(),
						PortfolioNumbers.plain(position.quantity()),
						PortfolioNumbers.price(position.avgPriceLocal()),
						PortfolioNumbers.krw(position.costKrw()),
						PortfolioNumbers.krw(position.realizedKrw()),
						PortfolioNumbers.krw(position.dividendsKrw())))
				.toList();

		List<AccountBalanceResponse> balances = accountRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
				.map(account -> account.getAccountType() == AccountType.LOAN
						? new AccountBalanceResponse(account.getId(), null,
								PortfolioNumbers.krw(state.liabilityByAccount().getOrDefault(account.getId(), BigDecimal.ZERO)))
						: new AccountBalanceResponse(account.getId(),
								PortfolioNumbers.krw(state.cashByAccount().getOrDefault(account.getId(), BigDecimal.ZERO)), null))
				.toList();

		return new HoldingsResponse(positions, balances,
				PortfolioNumbers.krw(state.realizedProfit()), PortfolioNumbers.krw(state.dividendIncome()));
	}

	/**
	 * Validates one request against the type rules and returns an unsaved entity.
	 * Holding-dependent rules (no overselling, no over-repaying) are checked afterwards by replaying the account.
	 */
	private LedgerTransaction toTransaction(TransactionRequest request) {
		TransactionType type = request.type();
		if (type == TRANSFER_IN || type == TRANSFER_OUT) {
			throw new LedgerValidationException("이체는 /api/portfolio/transfers로 등록하세요");
		}
		Account account = findAccount(request.accountId());
		rejectFutureDate(request.tradeDate());

		boolean loanAccount = account.getAccountType() == AccountType.LOAN;
		boolean loanType = type == LOAN_DRAW || type == LOAN_REPAY;
		if (loanAccount != loanType) {
			throw new LedgerValidationException(loanAccount
					? "대출 계좌에는 LOAN_DRAW / LOAN_REPAY만 등록할 수 있습니다 (시작 잔액도 LOAN_DRAW)"
					: "LOAN_DRAW / LOAN_REPAY는 대출 계좌에만 등록할 수 있습니다");
		}

		boolean positionEntry = POSITION_TYPES.contains(type) || (type == OPENING && request.instrumentId() != null);
		Instrument instrument = null;
		if (positionEntry) {
			if (!account.getAccountType().isInvestment()) {
				throw new LedgerValidationException("종목 거래는 투자 계좌(위탁·ISA·연금저축·IRP)에서만 가능합니다");
			}
			if (request.instrumentId() == null) {
				throw new LedgerValidationException("종목을 선택하세요");
			}
			instrument = instrumentRepository.findById(request.instrumentId())
					.orElseThrow(() -> notFound("종목을 찾을 수 없습니다"));
		} else if (request.instrumentId() != null) {
			throw new LedgerValidationException("%s 거래에는 종목을 지정할 수 없습니다".formatted(type));
		}

		boolean priced = TRADE_TYPES.contains(type) || (type == OPENING && instrument != null);
		BigDecimal price = request.price();
		BigDecimal fxRate = BigDecimal.ONE;
		String memo = request.memo();
		if (Boolean.TRUE.equals(request.useLatestPrice())) {
			if (type != OPENING || instrument == null || !request.tradeDate().equals(LocalDate.now(ZONE)) || request.quantity() == null) {
				throw new LedgerValidationException("자동 기준가는 오늘 날짜의 보유 종목 기초잔고에만 사용할 수 있습니다");
			}
			if (request.price() != null || request.fxRate() != null || request.amount() != null
					|| (request.fee() != null && request.fee().signum() != 0) || (request.tax() != null && request.tax().signum() != 0)) {
				throw new LedgerValidationException("자동 기준가 사용 시 가격·환율·정산금액·수수료·세금은 비워 주세요");
			}
			OpeningBasis basis = marketDataService.openingBasis(instrument);
			price = basis.price();
			fxRate = basis.fxRate();
			memo = "[시작 기준가] " + (memo == null ? "" : memo);
			if (memo.length() > 500) throw new LedgerValidationException("자동 기준가 등록 시 메모는 480자 이내로 입력하세요");
		} else if (priced) {
			if (request.quantity() == null || request.price() == null) {
				throw new LedgerValidationException("수량과 가격을 입력하세요");
			}
			if (instrument.isForeignCurrency()) {
				if (request.fxRate() == null) {
					throw new LedgerValidationException("외화 종목은 체결 환율이 필요합니다");
				}
				fxRate = request.fxRate();
			}
		}

		BigDecimal fee = request.fee() == null ? BigDecimal.ZERO : krw(request.fee());
		BigDecimal tax = request.tax() == null ? BigDecimal.ZERO : krw(request.tax());
		BigDecimal amount = request.amount() != null
				? krw(request.amount())
				: computeAmount(type, priced, request.quantity(), price, fxRate, fee, tax);

		if (amount.signum() < 0) {
			throw new LedgerValidationException("정산금액이 음수입니다. 수수료·세금을 확인하세요");
		}
		if (amount.signum() == 0 && !priced) {
			throw new LedgerValidationException("금액은 0보다 커야 합니다");
		}

		return LedgerTransaction.builder()
				.accountId(account.getId())
				.type(type)
				.tradeDate(request.tradeDate())
				.instrumentId(instrument == null ? null : instrument.getId())
				.quantity(priced ? request.quantity() : null)
				.price(priced ? price : null)
				.fxRate(fxRate)
				.amount(amount)
				.fee(fee)
				.tax(tax)
				.memo(memo)
				.build();
	}

	private static BigDecimal computeAmount(
			TransactionType type, boolean priced, BigDecimal quantity, BigDecimal price,
			BigDecimal fxRate, BigDecimal fee, BigDecimal tax) {
		if (!priced) {
			throw new LedgerValidationException("금액을 입력하세요");
		}
		BigDecimal gross = quantity.multiply(price).multiply(fxRate);
		return krw(switch (type) {
			case BUY -> gross.add(fee);
			case SELL -> gross.subtract(fee).subtract(tax);
			default -> gross; // OPENING position: cost basis
		});
	}

	private static LedgerTransaction transferLeg(
			TransactionType type, Long accountId, TransferRequest request, BigDecimal amount, UUID groupId) {
		return LedgerTransaction.builder()
				.accountId(accountId)
				.type(type)
				.tradeDate(request.tradeDate())
				.amount(amount)
				.transferGroupId(groupId)
				.memo(request.memo())
				.build();
	}

	/** Replays the account after a change; throws (rolling back the change) if it oversells or over-repays. */
	private void verifyLedger(Long accountId) {
		HoldingsCalculator.replay(transactionRepository.findByAccountIdOrderByTradeDateAscIdAsc(accountId));
	}

	private static void rejectFutureDate(LocalDate date) {
		if (date.isAfter(LocalDate.now(ZONE))) {
			throw new LedgerValidationException("미래 날짜는 입력할 수 없습니다");
		}
	}

	private static BigDecimal krw(BigDecimal value) {
		return value.setScale(0, RoundingMode.HALF_UP);
	}

	/** Entries already captured by a snapshot are frozen history: correct them with an offsetting entry. */
	private void ensureEditable(LocalDateTime createdAt, String what) {
		snapshotService.lockedThrough()
				.filter(lockedThrough -> !createdAt.isAfter(lockedThrough))
				.ifPresent(lockedThrough -> {
					throw new LedgerValidationException(
							"%s 스냅샷에 반영된 %s 수정·삭제할 수 없습니다. 정정은 반대 거래로 입력하세요"
									.formatted(lockedThrough.format(LOCK_FORMAT), what));
				});
	}

	private Account findAccount(Long accountId) {
		return accountRepository.findById(accountId).orElseThrow(() -> notFound("계좌를 찾을 수 없습니다"));
	}

	private LedgerTransaction findTransaction(Long transactionId) {
		return transactionRepository.findById(transactionId).orElseThrow(() -> notFound("거래를 찾을 수 없습니다"));
	}

	private static ResponseStatusException notFound(String message) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
	}
}
