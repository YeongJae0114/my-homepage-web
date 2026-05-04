package com.myhome.backend.domain.llm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LlmMessageResponse(
		@NotBlank
		String id,
		@NotBlank
		String role,
		@NotBlank
		@Size(max = 4_000)
		String content,
		@NotBlank
		String createdAt
) {
}
