package com.myhome.backend.domain.monitoring.dto;

import java.time.LocalDateTime;

public record ServiceResponse(
		Long id,
		String name,
		String description,
		String baseUrl,
		String status,
		Double uptimePercentage,
		Long responseTimeMs,
		LocalDateTime lastCheckedAt
) {
}
