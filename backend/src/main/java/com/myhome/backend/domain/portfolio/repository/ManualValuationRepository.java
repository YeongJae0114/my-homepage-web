package com.myhome.backend.domain.portfolio.repository;

import com.myhome.backend.domain.portfolio.entity.ManualValuation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualValuationRepository extends JpaRepository<ManualValuation, Long> {

	List<ManualValuation> findByAccountIdOrderByValuationDateDesc(Long accountId);

	Optional<ManualValuation> findByAccountIdAndValuationDate(Long accountId, LocalDate valuationDate);

	boolean existsByUpdatedAtAfter(java.time.LocalDateTime updatedAt);
}
