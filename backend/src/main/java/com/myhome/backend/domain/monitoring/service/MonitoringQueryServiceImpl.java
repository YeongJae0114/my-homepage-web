package com.myhome.backend.domain.monitoring.service;

import com.myhome.backend.domain.monitoring.dto.MonitoringResponse;
import com.myhome.backend.domain.monitoring.dto.MonitoringServicesResponse;
import com.myhome.backend.domain.monitoring.dto.MonitoringServersResponse;
import com.myhome.backend.domain.monitoring.dto.MonitoringSummaryResponse;
import com.myhome.backend.domain.monitoring.dto.MetricResponses.CpuMetricResponse;
import com.myhome.backend.domain.monitoring.dto.MetricResponses.DiskMetricResponse;
import com.myhome.backend.domain.monitoring.dto.MetricResponses.MemoryMetricResponse;
import com.myhome.backend.domain.monitoring.dto.MetricResponses.ServerMetricsResponse;
import com.myhome.backend.domain.monitoring.dto.ServerResponse;
import com.myhome.backend.domain.monitoring.dto.ServiceResponse;
import com.myhome.backend.domain.monitoring.entity.MonitoringSnapshot;
import com.myhome.backend.domain.monitoring.entity.MonitoredService;
import com.myhome.backend.domain.monitoring.entity.Server;
import com.myhome.backend.domain.monitoring.entity.ServerStatus;
import com.myhome.backend.domain.monitoring.entity.ServiceStatus;
import com.myhome.backend.domain.monitoring.repository.MonitoredServiceRepository;
import com.myhome.backend.domain.monitoring.repository.MonitoringSnapshotRepository;
import com.myhome.backend.domain.monitoring.repository.ServerRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringQueryServiceImpl implements MonitoringQueryService {

	private static final ZoneId RESPONSE_ZONE_ID = ZoneId.of("Asia/Seoul");
	private static final double BYTES_PER_GB = 1024.0 * 1024.0 * 1024.0;

	private final ServerRepository serverRepository;
	private final MonitoredServiceRepository monitoredServiceRepository;
	private final MonitoringSnapshotRepository monitoringSnapshotRepository;

	@Override
	public MonitoringResponse getMonitoring() {
		List<ServerResponse> serverResponses = getServerResponses();
		List<ServiceResponse> serviceResponses = getServiceResponses();

		return new MonitoringResponse(toSummary(serverResponses, serviceResponses), serverResponses, serviceResponses);
	}

	@Override
	public MonitoringServersResponse getMonitoringServers() {
		return new MonitoringServersResponse(getServerResponses());
	}

	@Override
	public MonitoringServicesResponse getMonitoringServices() {
		return new MonitoringServicesResponse(getServiceResponses());
	}

	private List<ServerResponse> getServerResponses() {
		List<Server> servers = serverRepository.findByEnabledTrueOrderByDisplayOrderAsc();
		Map<Long, MonitoringSnapshot> latestSnapshots = servers.stream()
				.map(server -> monitoringSnapshotRepository.findTopByServerOrderByCollectedAtDesc(server).orElse(null))
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(snapshot -> snapshot.getServer().getId(), Function.identity()));

		return servers.stream()
				.map(server -> toServerResponse(server, latestSnapshots.get(server.getId())))
				.toList();
	}

	private List<ServiceResponse> getServiceResponses() {
		return monitoredServiceRepository.findAllByOrderByNameAsc().stream()
				.map(this::toServiceResponse)
				.toList();
	}

	private ServerResponse toServerResponse(Server server, MonitoringSnapshot snapshot) {
		ServerStatus status = snapshot == null ? ServerStatus.UNKNOWN : snapshot.getStatus();
		return new ServerResponse(
				server.getName(),
				server.getName(),
				server.getDescription(),
				toFrontendServerRole(server.getRole()),
				toServerStatus(status),
				0L,
				toOffsetDateTime(snapshot == null ? null : snapshot.getCollectedAt()),
				List.copyOf(server.getTags()),
				toMetricsResponse(snapshot)
		);
	}

	private ServiceResponse toServiceResponse(MonitoredService service) {
		return new ServiceResponse(
				service.getKey(),
				service.getName(),
				service.getDescription(),
				service.getServer().getName(),
				service.getType(),
				toServiceStatus(service.getStatus()),
				service.getBaseUrl(),
				service.getPublicVisible(),
				toFrontendUptime(service),
				service.getResponseTimeMs(),
				toOffsetDateTime(service.getLastCheckedAt()),
				List.copyOf(service.getTags())
		);
	}

	private MonitoringSummaryResponse toSummary(List<ServerResponse> servers, List<ServiceResponse> services) {
		long onlineCount = countServerStatus(servers, "online") + countServiceStatus(services, "online");
		long degradedCount = countServerStatus(servers, "degraded") + countServiceStatus(services, "degraded");
		long offlineCount = countServerStatus(servers, "offline") + countServiceStatus(services, "offline");
		long standbyCount = countServerStatus(servers, "standby") + countServiceStatus(services, "standby");
		String status = determineOverallStatus(degradedCount, offlineCount, standbyCount);
		return new MonitoringSummaryResponse(
				status,
				toSummaryMessage(status, onlineCount, degradedCount, offlineCount, standbyCount),
				Stream.concat(
								servers.stream().map(ServerResponse::lastCheckedAt),
								services.stream().map(ServiceResponse::lastCheckedAt)
						)
						.filter(Objects::nonNull)
						.max(OffsetDateTime::compareTo)
						.orElse(null)
		);
	}

	private long countServerStatus(List<ServerResponse> servers, String status) {
		return servers.stream()
				.filter(server -> status.equals(server.status()))
				.count();
	}

	private long countServiceStatus(List<ServiceResponse> services, String status) {
		return services.stream()
				.filter(service -> status.equals(service.status()))
				.count();
	}

	private String determineOverallStatus(long degradedCount, long offlineCount, long standbyCount) {
		if (offlineCount > 0) {
			return "incident";
		}
		if (degradedCount > 0 || standbyCount > 0) {
			return "degraded";
		}
		return "operational";
	}

	private String toSummaryMessage(String status, long onlineCount, long degradedCount, long offlineCount, long standbyCount) {
		return switch (status) {
			case "incident" -> "Some monitored systems are offline";
			case "degraded" -> "Some monitored systems need attention";
			default -> "All monitored systems are online";
		};
	}

	private ServerMetricsResponse toMetricsResponse(MonitoringSnapshot snapshot) {
		if (snapshot == null) {
			return null;
		}
		CpuMetricResponse cpu = toCpuMetricResponse(snapshot);
		MemoryMetricResponse memory = toMemoryMetricResponse(snapshot);
		DiskMetricResponse disk = toDiskMetricResponse(snapshot);
		if (cpu == null && memory == null && disk == null) {
			return null;
		}
		return new ServerMetricsResponse(cpu, memory, disk);
	}

	private CpuMetricResponse toCpuMetricResponse(MonitoringSnapshot snapshot) {
		if (snapshot.getCpuUsagePercent() == null) {
			return null;
		}
		return new CpuMetricResponse(snapshot.getCpuUsagePercent());
	}

	private MemoryMetricResponse toMemoryMetricResponse(MonitoringSnapshot snapshot) {
		if (snapshot.getMemoryUsagePercent() == null
				&& snapshot.getMemoryAvailableBytes() == null
				&& snapshot.getMemoryTotalBytes() == null) {
			return null;
		}
		return new MemoryMetricResponse(
				snapshot.getMemoryUsagePercent(),
				toGigabytes(snapshot.getMemoryAvailableBytes()),
				toGigabytes(snapshot.getMemoryTotalBytes())
		);
	}

	private DiskMetricResponse toDiskMetricResponse(MonitoringSnapshot snapshot) {
		if (snapshot.getDiskUsagePercent() == null
				&& snapshot.getDiskAvailableBytes() == null
				&& snapshot.getDiskTotalBytes() == null) {
			return null;
		}
		return new DiskMetricResponse(
				snapshot.getDiskUsagePercent(),
				toGigabytes(snapshot.getDiskAvailableBytes()),
				toGigabytes(snapshot.getDiskTotalBytes())
		);
	}

	private Double toGigabytes(Double bytes) {
		if (bytes == null) {
			return null;
		}
		return Math.round((bytes / BYTES_PER_GB) * 100.0) / 100.0;
	}

	private OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return dateTime.atZone(RESPONSE_ZONE_ID).toOffsetDateTime();
	}

	private String toServerStatus(ServerStatus status) {
		return switch (status) {
			case ONLINE -> "online";
			case DEGRADED -> "degraded";
			case OFFLINE -> "offline";
			case UNKNOWN -> "standby";
		};
	}

	private String toServiceStatus(ServiceStatus status) {
		return switch (status) {
			case OK -> "online";
			case DEGRADED -> "degraded";
			case DOWN -> "offline";
			case UNKNOWN -> "standby";
		};
	}

	private String toFrontendServerRole(String role) {
		return switch (role) {
			case "cache" -> "redis";
			case "database" -> "db";
			case "toy-host" -> "lab";
			default -> role;
		};
	}

	private Double toFrontendUptime(MonitoredService service) {
		if (service.getUptimePercentage() != null) {
			return service.getUptimePercentage();
		}
		return switch (service.getStatus()) {
			case OK -> 100.0;
			case DEGRADED -> 99.0;
			case DOWN, UNKNOWN -> 0.0;
		};
	}
}
