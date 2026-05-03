package com.myhome.backend.domain.llm.service;

import com.myhome.backend.domain.llm.dto.LlmChatRequest;
import com.myhome.backend.domain.llm.dto.LlmChatResponse;
import com.myhome.backend.domain.llm.dto.LlmStatusResponse;
import com.myhome.backend.domain.llm.dto.LlmWakeResponse;

public interface LlmService {

	LlmStatusResponse getStatus();

	LlmWakeResponse wake();

	LlmChatResponse chat(LlmChatRequest request);
}
