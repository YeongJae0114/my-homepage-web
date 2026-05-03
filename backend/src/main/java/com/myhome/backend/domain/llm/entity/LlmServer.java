package com.myhome.backend.domain.llm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "llm_servers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LlmServer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 255)
	private String host;

	@Column(length = 30)
	private String macAddress;

	@Column(length = 500)
	private String chatEndpointUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private LlmServerStatus status;

	@Column
	private LocalDateTime lastWakeRequestedAt;

	@Column
	private LocalDateTime lastCheckedAt;

	@Builder
	private LlmServer(
			String name,
			String host,
			String macAddress,
			String chatEndpointUrl,
			LlmServerStatus status,
			LocalDateTime lastWakeRequestedAt,
			LocalDateTime lastCheckedAt
	) {
		this.name = name;
		this.host = host;
		this.macAddress = macAddress;
		this.chatEndpointUrl = chatEndpointUrl;
		this.status = status;
		this.lastWakeRequestedAt = lastWakeRequestedAt;
		this.lastCheckedAt = lastCheckedAt;
	}
}
