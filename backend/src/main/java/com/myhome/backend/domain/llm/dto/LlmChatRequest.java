package com.myhome.backend.domain.llm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record LlmChatRequest(
		@NotEmpty
		@Size(max = 50)
		List<@Valid LlmMessageResponse> messages
) {
}
