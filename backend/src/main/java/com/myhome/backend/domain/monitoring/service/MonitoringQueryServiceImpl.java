package com.myhome.backend.domain.monitoring.service;

import com.myhome.backend.domain.monitoring.dto.MonitoringResponse;
import com.myhome.backend.domain.monitoring.dto.ServerResponse;
import com.myhome.backend.domain.monitoring.dto.ServiceResponse;
import com.myhome.backend.domain.monitoring.entity.MonitoredService;
import com.myhome.backend.domain.monitoring.entity.Server;
import com.myhome.backend.domain.monitoring.repository.ServerRepository;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringQueryServiceImpl implements MonitoringQueryService {

	private final ServerRepository serverRepository;

	@Override
	public MonitoringResponse getMonitoring() {
		// TODO: DB 연동 및 비즈니스 로직 구현 - 실시간 상태 수집/캐싱 정책 확정 후 보강
		return new MonitoringResponse(
				serverRepository.findAll().stream()
						.sorted(Comparator.comparing(Server::getName))
						.map(this::toServerResponse)
						.toList()
		);
	}

	private ServerResponse toServerResponse(Server server) {
		return new ServerResponse(
				server.getId(),
				server.getName(),
				server.getHost(),
				server.getLocation(),
				server.getStatus().name().toLowerCase(),
				server.getCpuUsagePercent(),
				server.getMemoryUsagePercent(),
				server.getDiskUsagePercent(),
				server.getLastCheckedAt(),
				server.getServices().stream()
						.sorted(Comparator.comparing(MonitoredService::getName))
						.map(this::toServiceResponse)
						.toList()
		);
	}

	private ServiceResponse toServiceResponse(MonitoredService service) {
		return new ServiceResponse(
				service.getId(),
				service.getName(),
				service.getDescription(),
				service.getBaseUrl(),
				service.getStatus().name().toLowerCase(),
				service.getUptimePercentage(),
				service.getResponseTimeMs(),
				service.getLastCheckedAt()
		);
	}
}
