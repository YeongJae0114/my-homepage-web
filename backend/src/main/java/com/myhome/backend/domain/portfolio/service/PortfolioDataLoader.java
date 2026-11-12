package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.entity.FxRate;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.ManualValuation;
import com.myhome.backend.domain.portfolio.entity.Price;
import com.myhome.backend.domain.portfolio.repository.AccountRepository;
import com.myhome.backend.domain.portfolio.repository.FxRateRepository;
import com.myhome.backend.domain.portfolio.repository.InstrumentRepository;
import com.myhome.backend.domain.portfolio.repository.LedgerTransactionRepository;
import com.myhome.backend.domain.portfolio.repository.ManualValuationRepository;
import com.myhome.backend.domain.portfolio.repository.PriceRepository;
import com.myhome.backend.domain.portfolio.service.PortfolioValuator.Inputs;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Loads everything the valuator needs in one pass. */
@Component
@RequiredArgsConstructor
public class PortfolioDataLoader {

	private final AccountRepository accountRepository;
	private final InstrumentRepository instrumentRepository;
	private final LedgerTransactionRepository transactionRepository;
	private final PriceRepository priceRepository;
	private final FxRateRepository fxRateRepository;
	private final ManualValuationRepository valuationRepository;

	@Transactional(readOnly = true)
	public Inputs load() {
		return new Inputs(
				accountRepository.findAllByOrderByDisplayOrderAscIdAsc(),
				instrumentRepository.findAll().stream().collect(Collectors.toMap(Instrument::getId, Function.identity())),
				transactionRepository.findAllByOrderByTradeDateAscIdAsc(),
				series(priceRepository.findAll(), Price::getInstrumentId, Price::getPriceDate, Function.identity()),
				series(fxRateRepository.findAll(), FxRate::getCurrency, FxRate::getRateDate, FxRate::getRateToKrw),
				series(valuationRepository.findAll(), ManualValuation::getAccountId,
						ManualValuation::getValuationDate, ManualValuation::getAmount));
	}

	private static <T, K, V> Map<K, NavigableMap<LocalDate, V>> series(
			List<T> rows, Function<T, K> key, Function<T, LocalDate> date, Function<T, V> value) {
		return rows.stream().collect(Collectors.groupingBy(key,
				Collectors.toMap(date, value, (first, second) -> second, TreeMap::new)));
	}
}
