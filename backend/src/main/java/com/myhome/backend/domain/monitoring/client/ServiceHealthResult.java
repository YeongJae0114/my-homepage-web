package com.myhome.backend.domain.monitoring.client;

public record ServiceHealthResult(
		boolean online,
		Long responseTimeMs
) {
}
