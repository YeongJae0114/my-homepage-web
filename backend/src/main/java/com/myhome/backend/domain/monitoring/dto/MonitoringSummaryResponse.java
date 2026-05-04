package com.myhome.backend.domain.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MonitoringSummaryResponse(
		String status,
		String message,
		OffsetDateTime lastUpdatedAt
) {
}
