package com.myhome.backend.domain.llm.dto;

import java.time.LocalDateTime;

public record LlmChatResponse(
		String answer,
		String model,
		String status,
		LocalDateTime respondedAt
) {
}
