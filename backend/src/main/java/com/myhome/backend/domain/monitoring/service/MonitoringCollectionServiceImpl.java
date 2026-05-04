package com.myhome.backend.domain.monitoring.service;

import com.myhome.backend.domain.monitoring.client.NodeExporterClient;
import com.myhome.backend.domain.monitoring.client.NodeExporterMetricParser;
import com.myhome.backend.domain.monitoring.client.NodeExporterMetrics;
import com.myhome.backend.domain.monitoring.client.ServiceHealthClient;
import com.myhome.backend.domain.monitoring.client.ServiceHealthResult;
import com.myhome.backend.domain.monitoring.config.MonitoringProperties;
import com.myhome.backend.domain.monitoring.entity.MonitoringSnapshot;
import com.myhome.backend.domain.monitoring.entity.MonitoredService;
import com.myhome.backend.domain.monitoring.entity.Server;
import com.myhome.backend.domain.monitoring.entity.ServerStatus;
import com.myhome.backend.domain.monitoring.entity.ServiceStatus;
import com.myhome.backend.domain.monitoring.repository.MonitoredServiceRepository;
import com.myhome.backend.domain.monitoring.repository.MonitoringSnapshotRepository;
import com.myhome.backend.domain.monitoring.repository.ServerRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringCollectionServiceImpl implements MonitoringCollectionService {

	private static final double CPU_DEGRADED_THRESHOLD = 85.0;
	private static final double MEMORY_DEGRADED_THRESHOLD = 90.0;
	private static final double DISK_DEGRADED_THRESHOLD = 90.0;

	private final ServerRepository serverRepository;
	private final MonitoredServiceRepository monitoredServiceRepository;
	private final MonitoringSnapshotRepository monitoringSnapshotRepository;
	private final NodeExporterClient nodeExporterClient;
	private final NodeExporterMetricParser nodeExporterMetricParser;
	private final ServiceHealthClient serviceHealthClient;
	private final MonitoringProperties monitoringProperties;

	@Override
	@Transactional
	public void collect() {
		LocalDateTime collectedAt = LocalDateTime.now();

		serverRepository.findByEnabledTrueOrderByDisplayOrderAsc()
				.forEach(server -> collectServer(server, collectedAt));

		monitoredServiceRepository.findAllByOrderByNameAsc()
				.forEach(service -> collectService(service, collectedAt));

		LocalDateTime retentionCutoff = collectedAt.minus(monitoringProperties.retention().period());
		long deletedCount = monitoringSnapshotRepository.deleteByCollectedAtBefore(retentionCutoff);
		if (deletedCount > 0) {
			log.info("Deleted {} monitoring snapshots older than {}", deletedCount, retentionCutoff);
		}
	}

	private void collectServer(Server server, LocalDateTime collectedAt) {
		try {
			String metricsText = nodeExporterClient.fetchMetrics(server.getMetricsUrl());
			NodeExporterMetrics metrics = nodeExporterMetricParser.parse(metricsText);
			MonitoringSnapshot previousSnapshot = monitoringSnapshotRepository.findTopByServerOrderByCollectedAtDesc(server)
					.orElse(null);
			Double cpuUsagePercent = calculateCpuUsagePercent(metrics, previousSnapshot);
			ServerStatus status = determineStatus(cpuUsagePercent, metrics);

			monitoringSnapshotRepository.save(MonitoringSnapshot.builder()
					.server(server)
					.status(status)
					.cpuUsagePercent(cpuUsagePercent)
					.memoryUsagePercent(metrics.memoryUsagePercent())
					.memoryAvailableBytes(metrics.memoryAvailableBytes())
					.memoryTotalBytes(metrics.memoryTotalBytes())
					.diskUsagePercent(metrics.diskUsagePercent())
					.diskAvailableBytes(metrics.diskAvailableBytes())
					.diskTotalBytes(metrics.diskTotalBytes())
					.uptimeSeconds(metrics.uptimeSeconds())
					.loadAverage1m(metrics.loadAverage1m())
					.temperatureCelsius(metrics.temperatureCelsius())
					.cpuTotalSeconds(metrics.cpuTotalSeconds())
					.cpuIdleSeconds(metrics.cpuIdleSeconds())
					.collectedAt(collectedAt)
					.build());
		} catch (RuntimeException exception) {
			log.warn("Failed to collect node exporter metrics from {} ({})", server.getName(), server.getMetricsUrl(), exception);
			monitoringSnapshotRepository.save(MonitoringSnapshot.builder()
					.server(server)
					.status(ServerStatus.OFFLINE)
					.collectedAt(collectedAt)
					.errorMessage(exception.getMessage())
					.build());
		}
	}

	private void collectService(MonitoredService service, LocalDateTime collectedAt) {
		String healthCheckUrl = service.getHealthCheckUrl() == null ? service.getBaseUrl() : service.getHealthCheckUrl();
		ServiceHealthResult result = serviceHealthClient.check(healthCheckUrl);
		service.updateHealth(
				result.online() ? ServiceStatus.OK : ServiceStatus.DOWN,
				result.responseTimeMs(),
				collectedAt
		);
	}

	private Double calculateCpuUsagePercent(NodeExporterMetrics metrics, MonitoringSnapshot previousSnapshot) {
		if (metrics.cpuTotalSeconds() == null || metrics.cpuIdleSeconds() == null || previousSnapshot == null) {
			return null;
		}
		if (previousSnapshot.getCpuTotalSeconds() == null || previousSnapshot.getCpuIdleSeconds() == null) {
			return null;
		}

		double totalDelta = metrics.cpuTotalSeconds() - previousSnapshot.getCpuTotalSeconds();
		double idleDelta = metrics.cpuIdleSeconds() - previousSnapshot.getCpuIdleSeconds();
		if (totalDelta <= 0 || idleDelta < 0) {
			return null;
		}

		double cpuUsageRatio = 1.0 - (idleDelta / totalDelta);
		return Math.round(cpuUsageRatio * 10_000.0) / 100.0;
	}

	private ServerStatus determineStatus(Double cpuUsagePercent, NodeExporterMetrics metrics) {
		if (isGreaterThanOrEqual(cpuUsagePercent, CPU_DEGRADED_THRESHOLD)
				|| isGreaterThanOrEqual(metrics.memoryUsagePercent(), MEMORY_DEGRADED_THRESHOLD)
				|| isGreaterThanOrEqual(metrics.diskUsagePercent(), DISK_DEGRADED_THRESHOLD)) {
			return ServerStatus.DEGRADED;
		}
		return ServerStatus.ONLINE;
	}

	private boolean isGreaterThanOrEqual(Double value, double threshold) {
		return value != null && value >= threshold;
	}
}
