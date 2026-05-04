package com.myhome.backend.domain.monitoring.repository;

import com.myhome.backend.domain.monitoring.entity.MonitoredService;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoredServiceRepository extends JpaRepository<MonitoredService, Long> {

	List<MonitoredService> findByServerIdOrderByNameAsc(Long serverId);

	List<MonitoredService> findAllByOrderByNameAsc();
}
