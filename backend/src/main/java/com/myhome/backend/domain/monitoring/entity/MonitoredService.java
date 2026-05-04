package com.myhome.backend.domain.monitoring.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

	@Column(name = "service_key", nullable = false, length = 80)
	private String key;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(nullable = false, length = 500)
	private String baseUrl;

	@Column(length = 500)
	private String healthCheckUrl;

	@Column(name = "service_type", nullable = false, length = 80)
	private String type;

	@Column(nullable = false)
	private Boolean publicVisible;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ServiceStatus status;

	@Column
	private Double uptimePercentage;

	@Column
	private Long responseTimeMs;

	@Column
	private LocalDateTime lastCheckedAt;

	@ElementCollection
	@CollectionTable(name = "monitored_service_tags", joinColumns = @JoinColumn(name = "service_id"))
	@OrderColumn(name = "tag_order")
	@Column(name = "tag", nullable = false, length = 80)
	private List<String> tags = new ArrayList<>();

	@Builder
	private MonitoredService(
			Server server,
			String key,
			String name,
			String description,
			String baseUrl,
			String healthCheckUrl,
			String type,
			Boolean publicVisible,
			ServiceStatus status,
			Double uptimePercentage,
			Long responseTimeMs,
			LocalDateTime lastCheckedAt,
			List<String> tags
	) {
		this.server = server;
		this.key = key;
		this.name = name;
		this.description = description;
		this.baseUrl = baseUrl;
		this.healthCheckUrl = healthCheckUrl;
		this.type = type;
		this.publicVisible = publicVisible;
		this.status = status;
		this.uptimePercentage = uptimePercentage;
		this.responseTimeMs = responseTimeMs;
		this.lastCheckedAt = lastCheckedAt;
		this.tags = tags == null ? new ArrayList<>() : tags;
	}

	public void updateHealth(ServiceStatus status, Long responseTimeMs, LocalDateTime lastCheckedAt) {
		this.status = status;
		this.responseTimeMs = responseTimeMs;
		this.lastCheckedAt = lastCheckedAt;
	}
}
