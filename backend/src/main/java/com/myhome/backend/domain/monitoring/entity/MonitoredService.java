package com.myhome.backend.domain.monitoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "monitored_services")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonitoredService {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "server_id", nullable = false)
	private Server server;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(nullable = false, length = 500)
	private String baseUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ServiceStatus status;

	@Column
	private Double uptimePercentage;

	@Column
	private Long responseTimeMs;

	@Column
	private LocalDateTime lastCheckedAt;

	@Builder
	private MonitoredService(
			Server server,
			String name,
			String description,
			String baseUrl,
			ServiceStatus status,
			Double uptimePercentage,
			Long responseTimeMs,
			LocalDateTime lastCheckedAt
	) {
		this.server = server;
		this.name = name;
		this.description = description;
		this.baseUrl = baseUrl;
		this.status = status;
		this.uptimePercentage = uptimePercentage;
		this.responseTimeMs = responseTimeMs;
		this.lastCheckedAt = lastCheckedAt;
	}
}
