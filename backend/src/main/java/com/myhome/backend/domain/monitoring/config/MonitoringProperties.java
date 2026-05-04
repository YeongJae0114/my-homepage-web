package com.myhome.backend.domain.monitoring.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monitoring")
public record MonitoringProperties(
		Collection collection,
		Retention retention
) {

	public record Collection(
			boolean enabled,
			Duration fixedDelay,
			Duration initialDelay,
			Duration requestTimeout
	) {
	}

	public record Retention(Duration period) {
	}
}
