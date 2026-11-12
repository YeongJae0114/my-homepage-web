package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.entity.PortfolioSnapshot;
import com.myhome.backend.domain.portfolio.repository.AccountRepository;
import com.myhome.backend.domain.portfolio.repository.FxRateRepository;
import com.myhome.backend.domain.portfolio.repository.LedgerTransactionRepository;
import com.myhome.backend.domain.portfolio.repository.ManualValuationRepository;
import com.myhome.backend.domain.portfolio.repository.PortfolioSnapshotRepository;
import com.myhome.backend.domain.portfolio.repository.PriceRepository;
import com.myhome.backend.domain.portfolio.service.PortfolioValuator.Inputs;
import com.myhome.backend.domain.portfolio.service.PortfolioValuator.Result;
import com.myhome.backend.domain.portfolio.service.ReturnCalculator.CashFlow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Daily snapshots are write-once history: never recomputed after capture (today's may be recaptured).
 * Ledger entries created before the latest capture are locked (see {@link #lockedThrough()}), so a frozen
 * snapshot always matches the books it was taken from. Backdated entries made later show up from the next snapshot.
 */
@Service
@RequiredArgsConstructor
public class SnapshotService {

	private final PortfolioDataLoader dataLoader;
	private final AccountRepository accountRepository;
	private final PortfolioSnapshotRepository snapshotRepository;
	private final PriceRepository priceRepository;
	private final FxRateRepository fxRateRepository;
	private final LedgerTransactionRepository transactionRepository;
	private final ManualValuationRepository valuationRepository;

	/** Creates (or recaptures) the snapshot for the date. Only called with today's date. */
	@Transactional
	public PortfolioSnapshot capture(LocalDate date) {
		accountRepository.lockAllForLedgerWrite();
		return captureCurrent(date);
	}

	private PortfolioSnapshot captureCurrent(LocalDate date) {
		Inputs inputs = dataLoader.load();
		Result result = PortfolioValuator.valueAt(inputs, date);
		BigDecimal cumulativeFlow = PortfolioValuator.scopeFlows(inputs, null, date).stream()
				.map(CashFlow::amount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		// Difference of cumulative totals, not flows dated inside the window: a deposit backdated after the
		// previous capture is still counted here as a contribution, never as a return.
		BigDecimal previousCumulative = snapshotRepository.findTopBySnapshotDateLessThanOrderBySnapshotDateDesc(date)
				.map(PortfolioSnapshot::getScopeCumulativeFlow)
				.orElse(BigDecimal.ZERO);

		PortfolioSnapshot snapshot = snapshotRepository.findBySnapshotDate(date).orElseGet(() -> new PortfolioSnapshot(date));
		snapshot.record(
				money(result.cashEquivalent()),
				money(result.investment()),
				money(result.pensionInsurance()),
				money(result.totalLiabilities()),
				money(result.scopeValue()),
				money(cumulativeFlow.subtract(previousCumulative)),
				money(cumulativeFlow),
				result.priceAsOf(),
				LocalDateTime.now());
		return snapshotRepository.save(snapshot);
	}

	/** Scheduled capture: skipped when nothing changed since the last snapshot (holidays need no calendar). */
	@Transactional
	public boolean captureIfChanged(LocalDate date) {
		accountRepository.lockAllForLedgerWrite();
		Optional<LocalDateTime> since = lockedThrough();
		if (since.isPresent()) {
			boolean changed = priceRepository.existsByCapturedAtAfter(since.get())
					|| fxRateRepository.existsByCapturedAtAfter(since.get())
					|| transactionRepository.existsByUpdatedAtAfter(since.get())
					|| valuationRepository.existsByUpdatedAtAfter(since.get());
			if (!changed) {
				return false;
			}
		}
		captureCurrent(date);
		return true;
	}

	/** Entries created at or before this moment are part of frozen history. Empty before the first snapshot. */
	@Transactional(readOnly = true)
	public Optional<LocalDateTime> lockedThrough() {
		return snapshotRepository.findTopByOrderBySnapshotDateDesc().map(PortfolioSnapshot::getComputedAt);
	}

	private static BigDecimal money(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}
}
