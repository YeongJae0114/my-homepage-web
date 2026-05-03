package com.myhome.backend.domain.monitoring.controller;

import com.myhome.backend.domain.monitoring.dto.MonitoringResponse;
import com.myhome.backend.domain.monitoring.service.MonitoringQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monitoring")
public class MonitoringController {

	private final MonitoringQueryService monitoringQueryService;

	@GetMapping
	public MonitoringResponse getMonitoring() {
		return monitoringQueryService.getMonitoring();
	}
}
