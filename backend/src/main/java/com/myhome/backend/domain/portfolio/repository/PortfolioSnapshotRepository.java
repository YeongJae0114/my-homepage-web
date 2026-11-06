package com.myhome.backend.domain.portfolio.repository;

import com.myhome.backend.domain.portfolio.entity.PortfolioSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

	Optional<PortfolioSnapshot> findBySnapshotDate(LocalDate snapshotDate);

	Optional<PortfolioSnapshot> findTopByOrderBySnapshotDateDesc();

	Optional<PortfolioSnapshot> findTopBySnapshotDateLessThanOrderBySnapshotDateDesc(LocalDate snapshotDate);

	Optional<PortfolioSnapshot> findTopBySnapshotDateLessThanEqualOrderBySnapshotDateDesc(LocalDate snapshotDate);

	List<PortfolioSnapshot> findBySnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(LocalDate snapshotDate);

	List<PortfolioSnapshot> findAllByOrderBySnapshotDateAsc();
}
