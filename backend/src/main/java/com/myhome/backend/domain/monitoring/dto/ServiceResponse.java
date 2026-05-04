package com.myhome.backend.domain.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServiceResponse(
		String id,
		String name,
		String description,
		String serverId,
		String type,
		String status,
		String endpoint,
		Boolean isPublic,
		Double uptime,
		Long latencyMs,
		OffsetDateTime lastCheckedAt,
		List<String> tags
) {
}
