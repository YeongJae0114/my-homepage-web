package com.myhome.backend.domain.portfolio.repository;

import com.myhome.backend.domain.portfolio.entity.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

	List<Account> findAllByOrderByDisplayOrderAscIdAsc();
}
