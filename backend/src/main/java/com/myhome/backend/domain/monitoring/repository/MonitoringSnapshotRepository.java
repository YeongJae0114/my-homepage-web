package com.myhome.backend.domain.monitoring.repository;

import com.myhome.backend.domain.monitoring.entity.MonitoringSnapshot;
import com.myhome.backend.domain.monitoring.entity.Server;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoringSnapshotRepository extends JpaRepository<MonitoringSnapshot, Long> {

	Optional<MonitoringSnapshot> findTopByServerOrderByCollectedAtDesc(Server server);

	long deleteByCollectedAtBefore(LocalDateTime collectedAt);
}
