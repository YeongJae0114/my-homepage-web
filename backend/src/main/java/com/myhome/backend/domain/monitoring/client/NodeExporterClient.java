package com.myhome.backend.domain.monitoring.client;

import com.myhome.backend.domain.monitoring.config.MonitoringProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class NodeExporterClient {

	private final MonitoringProperties monitoringProperties;

	public String fetchMetrics(String metricsUrl) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(monitoringProperties.collection().requestTimeout());
		requestFactory.setReadTimeout(monitoringProperties.collection().requestTimeout());

		return RestClient.builder()
				.requestFactory(requestFactory)
				.build()
				.get()
				.uri(metricsUrl)
				.retrieve()
				.body(String.class);
	}
}
