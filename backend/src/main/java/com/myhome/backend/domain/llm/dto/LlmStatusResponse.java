package com.myhome.backend.domain.llm.dto;

import java.time.LocalDateTime;

public record LlmStatusResponse(
		String status,
		String host,
		LocalDateTime lastWakeRequestedAt,
		LocalDateTime lastCheckedAt
) {
}
