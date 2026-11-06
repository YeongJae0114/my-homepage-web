package com.myhome.backend.domain.portfolio.repository;

import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

	List<Instrument> findAllByOrderByNameAsc();

	Optional<Instrument> findByMarketAndSymbol(Market market, String symbol);
}
