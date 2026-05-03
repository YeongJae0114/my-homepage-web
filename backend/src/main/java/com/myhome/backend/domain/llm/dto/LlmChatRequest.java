package com.myhome.backend.domain.llm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LlmChatRequest(
		@NotBlank
		@Size(max = 4_000)
		String prompt
) {
}
