package com.myhome.backend.domain.portfolio.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/** External market-data sources. Blank keys mean "not connected yet": collectors skip, manual input still works. */
@ConfigurationProperties(prefix = "portfolio")
public record PortfolioProperties(Duration requestTimeout, Kis kis, Kexim kexim) {

	public record Kis(String baseUrl, String appKey, String appSecret) {

		public boolean configured() {
			return StringUtils.hasText(appKey) && StringUtils.hasText(appSecret);
		}
	}

	public record Kexim(String baseUrl, String apiKey) {

		public boolean configured() {
			return StringUtils.hasText(apiKey);
		}
	}
}
