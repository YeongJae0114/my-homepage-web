package com.myhome.backend.domain.portfolio.repository;

import com.myhome.backend.domain.portfolio.entity.Account;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Long> {

	List<Account> findAllByOrderByDisplayOrderAscIdAsc();

	/** Serialize writes that replay or freeze the owner's ledger, using the same lock order. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select account from Account account order by account.id")
	List<Account> lockAllForLedgerWrite();
}
