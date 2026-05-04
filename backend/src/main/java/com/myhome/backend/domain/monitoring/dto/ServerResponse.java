package com.myhome.backend.domain.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myhome.backend.domain.monitoring.dto.MetricResponses.ServerMetricsResponse;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServerResponse(
		String id,
		String name,
		String description,
		String role,
		String status,
		Long latencyMs,
		OffsetDateTime lastCheckedAt,
		List<String> tags,
		ServerMetricsResponse metrics
) {
}
