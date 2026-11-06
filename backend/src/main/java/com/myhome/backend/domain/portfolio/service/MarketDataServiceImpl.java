package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.client.IntegrationStatus;
import com.myhome.backend.domain.portfolio.client.KeximFxClient;
import com.myhome.backend.domain.portfolio.client.KisClient;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.FxInputRequest;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.FxItem;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.IntegrationsResponse;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.PriceInputRequest;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.PriceItem;
import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.SnapshotResponse;
import com.myhome.backend.domain.portfolio.entity.FxRate;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import com.myhome.backend.domain.portfolio.entity.Instrument.PriceSource;
import com.myhome.backend.domain.portfolio.entity.MarketDataSource;
import com.myhome.backend.domain.portfolio.entity.Price;
import com.myhome.backend.domain.portfolio.repository.FxRateRepository;
import com.myhome.backend.domain.portfolio.repository.InstrumentRepository;
import com.myhome.backend.domain.portfolio.repository.LedgerTransactionRepository;
import com.myhome.backend.domain.portfolio.repository.PriceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketDataServiceImpl implements MarketDataService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	private final PriceRepository priceRepository;
	private final FxRateRepository fxRateRepository;
	private final InstrumentRepository instrumentRepository;
	private final LedgerTransactionRepository transactionRepository;
	private final SnapshotService snapshotService;
	private final KisClient kisClient;
	private final KeximFxClient keximFxClient;
	private final IntegrationStatus integrationStatus;

	@Override
	@Transactional
	public void savePrices(PriceInputRequest request) {
		rejectFutureDate(request.date());
		LocalDateTime now = LocalDateTime.now();
		for (PriceItem item : request.items()) {
			if (!instrumentRepository.existsById(item.instrumentId())) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "종목을 찾을 수 없습니다 (#%d)".formatted(item.instrumentId()));
			}
			Price price = priceRepository.findByInstrumentIdAndPriceDate(item.instrumentId(), request.date())
					.orElseGet(() -> new Price(item.instrumentId(), request.date()));
			price.record(item.closePrice(), MarketDataSource.MANUAL, true, now);
			priceRepository.save(price);
		}
	}

	@Override
	@Transactional
	public void saveFxRates(FxInputRequest request) {
		rejectFutureDate(request.date());
		LocalDateTime now = LocalDateTime.now();
		for (FxItem item : request.items()) {
			if ("KRW".equals(item.currency())) {
				throw new LedgerValidationException("KRW 환율은 입력할 필요가 없습니다");
			}
			FxRate rate = fxRateRepository.findByCurrencyAndRateDate(item.currency(), request.date())
					.orElseGet(() -> new FxRate(item.currency(), request.date()));
			rate.record(item.rateToKrw(), MarketDataSource.MANUAL, now);
			fxRateRepository.save(rate);
		}
	}

	@Override
	@Transactional
	public SnapshotResponse captureSnapshot() {
		return SnapshotResponse.from(snapshotService.capture(LocalDate.now(ZONE)));
	}

	@Override
	public IntegrationsResponse getIntegrations() {
		return new IntegrationsResponse(
				integrationStatus.get(IntegrationStatus.KIS, kisClient.isConfigured()),
				integrationStatus.get(IntegrationStatus.KEXIM, keximFxClient.isConfigured()));
	}

	@Override
	public List<Instrument> collectionTargets(boolean domestic) {
		Set<Long> held = heldInstrumentIds();
		return instrumentRepository.findAllByOrderByNameAsc().stream()
				.filter(instrument -> held.contains(instrument.getId()))
				.filter(instrument -> instrument.isActive() && instrument.getPriceSource() == PriceSource.KIS)
				.filter(instrument -> (instrument.getMarket() == Market.KRX) == domestic)
				.toList();
	}

	@Override
	public Set<String> heldForeignCurrencies() {
		Set<Long> held = heldInstrumentIds();
		Set<String> currencies = new TreeSet<>();
		instrumentRepository.findAllById(held).stream()
				.filter(Instrument::isForeignCurrency)
				.forEach(instrument -> currencies.add(instrument.getTradingCurrency()));
		return currencies;
	}

	@Override
	@Transactional
	public boolean recordCollectedPrice(Long instrumentId, LocalDate date, BigDecimal closePrice, boolean finalized) {
		Price price = priceRepository.findByInstrumentIdAndPriceDate(instrumentId, date)
				.orElseGet(() -> new Price(instrumentId, date));
		if (price.getSource() == MarketDataSource.MANUAL) {
			return false;
		}
		price.record(closePrice, MarketDataSource.KIS, finalized, LocalDateTime.now());
		priceRepository.save(price);
		return true;
	}

	@Override
	@Transactional
	public boolean recordCollectedFx(String currency, LocalDate date, BigDecimal rateToKrw) {
		FxRate rate = fxRateRepository.findByCurrencyAndRateDate(currency, date)
				.orElseGet(() -> new FxRate(currency, date));
		if (rate.getSource() == MarketDataSource.MANUAL) {
			return false;
		}
		rate.record(rateToKrw, MarketDataSource.KEXIM, LocalDateTime.now());
		fxRateRepository.save(rate);
		return true;
	}

	private Set<Long> heldInstrumentIds() {
		Set<Long> held = new TreeSet<>();
		HoldingsCalculator.replay(transactionRepository.findAllByOrderByTradeDateAscIdAsc()).positions().values().stream()
				.filter(position -> position.quantity().signum() > 0)
				.forEach(position -> held.add(position.instrumentId()));
		return held;
	}

	private static void rejectFutureDate(LocalDate date) {
		if (date.isAfter(LocalDate.now(ZONE))) {
			throw new LedgerValidationException("미래 날짜는 입력할 수 없습니다");
		}
	}
}
