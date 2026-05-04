package com.myhome.backend.domain.monitoring.service;

import com.myhome.backend.domain.monitoring.dto.MonitoringResponse;
import com.myhome.backend.domain.monitoring.dto.MonitoringServicesResponse;
import com.myhome.backend.domain.monitoring.dto.MonitoringServersResponse;

public interface MonitoringQueryService {

	MonitoringResponse getMonitoring();

	MonitoringServersResponse getMonitoringServers();

	MonitoringServicesResponse getMonitoringServices();
}
