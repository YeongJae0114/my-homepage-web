package com.myhome.backend.domain.portfolio.controller;

import com.myhome.backend.domain.portfolio.dto.DashboardDtos.DashboardResponse;
import com.myhome.backend.domain.portfolio.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Owner-only: the whole dashboard in one call. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping
	public DashboardResponse getDashboard(@RequestParam(defaultValue = "1Y") String range) {
		return dashboardService.getDashboard(range);
	}
}
