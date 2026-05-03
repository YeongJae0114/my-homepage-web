package com.myhome.backend.domain.llm.service;

import com.myhome.backend.domain.llm.dto.LlmChatRequest;
import com.myhome.backend.domain.llm.dto.LlmChatResponse;
import com.myhome.backend.domain.llm.dto.LlmStatusResponse;
import com.myhome.backend.domain.llm.dto.LlmWakeResponse;
import com.myhome.backend.domain.llm.entity.LlmServer;
import com.myhome.backend.domain.llm.entity.LlmServerStatus;
import com.myhome.backend.domain.llm.repository.LlmServerRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LlmServiceImpl implements LlmService {

	private final LlmServerRepository llmServerRepository;

	@Override
	public LlmStatusResponse getStatus() {
		// TODO: DB 연동 및 비즈니스 로직 구현 - GPU 서버 헬스체크 결과와 저장된 상태 동기화
		return llmServerRepository.findFirstByOrderByIdAsc()
				.map(this::toStatusResponse)
				.orElseGet(() -> new LlmStatusResponse(
						LlmServerStatus.OFFLINE.name().toLowerCase(),
						null,
						null,
						null
				));
	}

	@Override
	@Transactional
	public LlmWakeResponse wake() {
		// TODO: DB 연동 및 비즈니스 로직 구현 - WOL(Wake-on-LAN) 매직 패킷 전송 로직 구현
		return new LlmWakeResponse(
				LlmServerStatus.WAKING.name().toLowerCase(),
				"Wake request accepted.",
				LocalDateTime.now()
		);
	}

	@Override
	public LlmChatResponse chat(LlmChatRequest request) {
		// TODO: DB 연동 및 비즈니스 로직 구현 - 외부 로컬 LLM 서버 HTTP/gRPC 통신 로직 구현
		return new LlmChatResponse(
				null,
				null,
				LlmServerStatus.OFFLINE.name().toLowerCase(),
				LocalDateTime.now()
		);
	}

	private LlmStatusResponse toStatusResponse(LlmServer server) {
		return new LlmStatusResponse(
				server.getStatus().name().toLowerCase(),
				server.getHost(),
				server.getLastWakeRequestedAt(),
				server.getLastCheckedAt()
		);
	}
}
