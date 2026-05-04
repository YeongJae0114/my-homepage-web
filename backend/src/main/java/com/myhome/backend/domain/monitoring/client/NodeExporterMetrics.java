package com.myhome.backend.domain.monitoring.client;

public record NodeExporterMetrics(
		Double cpuTotalSeconds,
		Double cpuIdleSeconds,
		Double memoryUsagePercent,
		Double memoryAvailableBytes,
		Double memoryTotalBytes,
		Double diskUsagePercent,
		Double diskAvailableBytes,
		Double diskTotalBytes,
		Long uptimeSeconds,
		Double loadAverage1m,
		Double temperatureCelsius
) {
}
