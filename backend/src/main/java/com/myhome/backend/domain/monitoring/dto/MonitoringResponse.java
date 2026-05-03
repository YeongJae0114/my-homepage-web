package com.myhome.backend.domain.monitoring.dto;

import java.util.List;

public record MonitoringResponse(
		List<ServerResponse> servers
) {
}
