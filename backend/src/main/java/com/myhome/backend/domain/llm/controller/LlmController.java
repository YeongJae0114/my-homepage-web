package com.myhome.backend.domain.llm.controller;

import com.myhome.backend.domain.llm.dto.LlmChatRequest;
import com.myhome.backend.domain.llm.dto.LlmChatResponse;
import com.myhome.backend.domain.llm.dto.LlmStatusResponse;
import com.myhome.backend.domain.llm.dto.LlmWakeResponse;
import com.myhome.backend.domain.llm.service.LlmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/llm")
public class LlmController {

	private final LlmService llmService;

	@GetMapping("/status")
	public LlmStatusResponse getStatus() {
		return llmService.getStatus();
	}

	@PostMapping("/wake")
	public LlmWakeResponse wake() {
		return llmService.wake();
	}

	@PostMapping("/chat")
	public LlmChatResponse chat(@Valid @RequestBody LlmChatRequest request) {
		return llmService.chat(request);
	}
}
