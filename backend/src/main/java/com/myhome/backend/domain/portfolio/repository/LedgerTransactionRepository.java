package com.myhome.backend.domain.portfolio.repository;

import com.myhome.backend.domain.portfolio.entity.LedgerTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LedgerTransactionRepository
		extends JpaRepository<LedgerTransaction, Long>, JpaSpecificationExecutor<LedgerTransaction> {

	/** Replay order: by trade date, then insertion order within a day. */
	List<LedgerTransaction> findAllByOrderByTradeDateAscIdAsc();

	List<LedgerTransaction> findByAccountIdOrderByTradeDateAscIdAsc(Long accountId);

	List<LedgerTransaction> findByTransferGroupId(UUID transferGroupId);

	boolean existsByAccountId(Long accountId);

	boolean existsByInstrumentId(Long instrumentId);

	boolean existsByUpdatedAtAfter(java.time.LocalDateTime updatedAt);
}
