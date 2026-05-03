package com.myhome.backend.domain.monitoring.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ServerResponse(
		Long id,
		String name,
		String host,
		String location,
		String status,
		Double cpuUsagePercent,
		Double memoryUsagePercent,
		Double diskUsagePercent,
		LocalDateTime lastCheckedAt,
		List<ServiceResponse> services
) {
}
