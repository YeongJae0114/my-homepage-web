package com.myhome.backend.domain.monitoring.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
@Table(name = "servers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Server {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(nullable = false, length = 255)
	private String host;

	@Column(length = 120)
	private String location;

	@Column(name = "server_role", nullable = false, length = 80)
	private String role;

	@Column(nullable = false, length = 500)
	private String metricsUrl;

	@Column(nullable = false)
	private Boolean enabled;

	@Column(nullable = false)
	private Integer displayOrder;

	@ElementCollection
	@CollectionTable(name = "server_tags", joinColumns = @JoinColumn(name = "server_id"))
	@OrderColumn(name = "tag_order")
	@Column(name = "tag", nullable = false, length = 80)
	private List<String> tags = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ServerStatus status;

	@Column
	private Double cpuUsagePercent;

	@Column
	private Double memoryUsagePercent;

	@Column
	private Double diskUsagePercent;

	@Column
	private LocalDateTime lastCheckedAt;

	@OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MonitoredService> services = new ArrayList<>();

	@Builder
	private Server(
			String name,
			String description,
			String host,
			String location,
			String role,
			String metricsUrl,
			Boolean enabled,
			Integer displayOrder,
			ServerStatus status,
			Double cpuUsagePercent,
			Double memoryUsagePercent,
			Double diskUsagePercent,
			LocalDateTime lastCheckedAt
	) {
		this.name = name;
		this.description = description;
		this.host = host;
		this.location = location;
		this.role = role;
		this.metricsUrl = metricsUrl;
		this.enabled = enabled;
		this.displayOrder = displayOrder;
		this.status = status;
		this.cpuUsagePercent = cpuUsagePercent;
		this.memoryUsagePercent = memoryUsagePercent;
		this.diskUsagePercent = diskUsagePercent;
		this.lastCheckedAt = lastCheckedAt;
	}
}
