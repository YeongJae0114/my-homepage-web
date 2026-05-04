package com.myhome.backend.domain.monitoring.client;

import com.myhome.backend.domain.monitoring.config.MonitoringProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ServiceHealthClient {

	private final MonitoringProperties monitoringProperties;

	public ServiceHealthResult check(String endpoint) {
		long startedAt = System.nanoTime();
		try {
			HttpStatusCode statusCode = restClient()
					.get()
					.uri(endpoint)
					.retrieve()
					.toBodilessEntity()
					.getStatusCode();

			long responseTimeMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
			return new ServiceHealthResult(statusCode.is2xxSuccessful() || statusCode.is3xxRedirection(), responseTimeMs);
		} catch (RuntimeException exception) {
			return new ServiceHealthResult(false, null);
		}
	}

	private RestClient restClient() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(monitoringProperties.collection().requestTimeout());
		requestFactory.setReadTimeout(monitoringProperties.collection().requestTimeout());
		return RestClient.builder()
				.requestFactory(requestFactory)
				.build();
	}
}
