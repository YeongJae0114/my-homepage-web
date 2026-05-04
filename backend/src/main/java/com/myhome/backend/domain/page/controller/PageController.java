package com.myhome.backend.domain.page.controller;

import com.myhome.backend.domain.page.dto.FrontendPageResponses.AboutResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.HomeResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ProjectPageResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ServicePageResponse;
import com.myhome.backend.domain.page.service.PageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PageController {

	private final PageQueryService pageQueryService;

	@GetMapping("/home")
	public HomeResponse getHome() {
		return pageQueryService.getHome();
	}

	@GetMapping("/about")
	public AboutResponse getAbout() {
		return pageQueryService.getAbout();
	}

	@GetMapping("/service")
	public ServicePageResponse getService() {
		return pageQueryService.getService();
	}

	@GetMapping("/project")
	public ProjectPageResponse getProject() {
		return pageQueryService.getProject();
	}
}
