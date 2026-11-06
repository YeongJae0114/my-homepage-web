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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
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
	private static final DateTimeFormatter LOCK_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

	private final AccountRepository accountRepository;
	private final InstrumentRepository instrumentRepository;
	private final LedgerTransactionRepository transactionRepository;
	private final ManualValuationRepository valuationRepository;
	private final SnapshotService snapshotService;

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
	@Transactional
	public TransactionResponse createTransaction(TransactionRequest request) {
		LedgerTransaction saved = transactionRepository.save(toTransaction(request));
		verifyLedger(saved.getAccountId());
		return TransactionResponse.from(saved, null);
	}

	@Override
	@Transactional
	public TransactionResponse updateTransaction(Long transactionId, TransactionRequest request) {
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
		List<LedgerTransaction> legs = transactionRepository.saveAll(List.of(
				transferLeg(TRANSFER_OUT, from.getId(), request, amount, groupId),
				transferLeg(TRANSFER_IN, to.getId(), request, amount, groupId)));
		return legs.stream().map(leg -> TransactionResponse.from(leg, null)).toList();
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
		BigDecimal fxRate = BigDecimal.ONE;
		if (priced) {
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
				: computeAmount(type, priced, request.quantity(), request.price(), fxRate, fee, tax);

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
				.price(priced ? request.price() : null)
				.fxRate(fxRate)
				.amount(amount)
				.fee(fee)
				.tax(tax)
				.memo(request.memo())
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
