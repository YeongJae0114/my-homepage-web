package com.myhome.backend.domain.llm.repository;

import com.myhome.backend.domain.llm.entity.LlmServer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LlmServerRepository extends JpaRepository<LlmServer, Long> {

	Optional<LlmServer> findFirstByOrderByIdAsc();
}
