package com.myhome.backend.domain.portfolio.repository;

import com.myhome.backend.domain.portfolio.entity.Price;
import java.time.LocalDateTime;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price, Long> {

	Optional<Price> findByInstrumentIdAndPriceDate(Long instrumentId, LocalDate priceDate);

	Optional<Price> findTopByInstrumentIdAndPriceDateLessThanEqualOrderByPriceDateDesc(Long instrumentId, LocalDate date);

	boolean existsByCapturedAtAfter(LocalDateTime capturedAt);
}
