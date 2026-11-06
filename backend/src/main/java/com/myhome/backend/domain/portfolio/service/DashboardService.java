package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.dto.DashboardDtos.DashboardResponse;

public interface DashboardService {

	/** range: 1M, 3M, 6M, 1Y or ALL (history window only; returns are always since inception). */
	DashboardResponse getDashboard(String range);
}
