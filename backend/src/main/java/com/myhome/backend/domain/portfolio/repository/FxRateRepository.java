package com.myhome.backend.domain.portfolio.repository;

import com.myhome.backend.domain.portfolio.entity.FxRate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxRateRepository extends JpaRepository<FxRate, Long> {

	Optional<FxRate> findByCurrencyAndRateDate(String currency, LocalDate rateDate);

	boolean existsByCapturedAtAfter(LocalDateTime capturedAt);
}
