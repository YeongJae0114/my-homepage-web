package com.myhome.backend.domain.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class MetricResponses {

	private MetricResponses() {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record ServerMetricsResponse(
			CpuMetricResponse cpu,
			MemoryMetricResponse memory,
			DiskMetricResponse disk
	) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record CpuMetricResponse(
			Double usedPercent
	) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record MemoryMetricResponse(
			Double usedPercent,
			Double availableGb,
			Double totalGb
	) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record DiskMetricResponse(
			Double usedPercent,
			Double availableGb,
			Double totalGb
	) {
	}
}
