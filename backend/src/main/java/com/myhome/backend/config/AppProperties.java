package com.myhome.backend.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
		Cors cors,
		Frontend frontend,
		External external
) {

	public record Cors(List<String> allowedOrigins) {
	}

	public record Frontend(String baseUrl) {
	}

	public record External(String velogGraphqlUrl, String githubApiUrl) {
	}
}
