package com.myhome.backend.domain.monitoring.scheduler;

import com.myhome.backend.domain.monitoring.config.MonitoringProperties;
import com.myhome.backend.domain.monitoring.service.MonitoringCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringCollectScheduler {

	private final MonitoringProperties monitoringProperties;
	private final MonitoringCollectionService monitoringCollectionService;

	@Scheduled(
			fixedDelayString = "${monitoring.collection.fixed-delay}",
			initialDelayString = "${monitoring.collection.initial-delay}"
	)
	public void collect() {
		if (!monitoringProperties.collection().enabled()) {
			log.debug("Monitoring collection is disabled.");
			return;
		}
		monitoringCollectionService.collect();
	}
}
