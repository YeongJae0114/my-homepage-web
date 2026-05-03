package com.myhome.backend.domain.llm.dto;

import java.time.LocalDateTime;

public record LlmWakeResponse(
		String status,
		String message,
		LocalDateTime requestedAt
) {
}
