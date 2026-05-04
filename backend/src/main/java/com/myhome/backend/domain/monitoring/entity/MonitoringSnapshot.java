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
@Table(name = "monitoring_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonitoringSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "server_id", nullable = false)
	private Server server;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ServerStatus status;

	@Column
	private Double cpuUsagePercent;

	@Column
	private Double memoryUsagePercent;

	@Column
	private Double memoryAvailableBytes;

	@Column
	private Double memoryTotalBytes;

	@Column
	private Double diskUsagePercent;

	@Column
	private Double diskAvailableBytes;

	@Column
	private Double diskTotalBytes;

	@Column
	private Long uptimeSeconds;

	@Column
	private Double loadAverage1m;

	@Column
	private Double temperatureCelsius;

	@Column
	private Double cpuTotalSeconds;

	@Column
	private Double cpuIdleSeconds;

	@Column(nullable = false)
	private LocalDateTime collectedAt;

	@Column(columnDefinition = "text")
	private String errorMessage;

	@Builder
	private MonitoringSnapshot(
			Server server,
			ServerStatus status,
			Double cpuUsagePercent,
			Double memoryUsagePercent,
			Double memoryAvailableBytes,
			Double memoryTotalBytes,
			Double diskUsagePercent,
			Double diskAvailableBytes,
			Double diskTotalBytes,
			Long uptimeSeconds,
			Double loadAverage1m,
			Double temperatureCelsius,
			Double cpuTotalSeconds,
			Double cpuIdleSeconds,
			LocalDateTime collectedAt,
			String errorMessage
	) {
		this.server = server;
		this.status = status;
		this.cpuUsagePercent = cpuUsagePercent;
		this.memoryUsagePercent = memoryUsagePercent;
		this.memoryAvailableBytes = memoryAvailableBytes;
		this.memoryTotalBytes = memoryTotalBytes;
		this.diskUsagePercent = diskUsagePercent;
		this.diskAvailableBytes = diskAvailableBytes;
		this.diskTotalBytes = diskTotalBytes;
		this.uptimeSeconds = uptimeSeconds;
		this.loadAverage1m = loadAverage1m;
		this.temperatureCelsius = temperatureCelsius;
		this.cpuTotalSeconds = cpuTotalSeconds;
		this.cpuIdleSeconds = cpuIdleSeconds;
		this.collectedAt = collectedAt;
		this.errorMessage = errorMessage;
	}
}
