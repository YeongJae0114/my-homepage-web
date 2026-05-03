package com.myhome.backend.domain.page.controller;

import com.myhome.backend.domain.page.dto.PageResponse;
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
	public PageResponse getHome() {
		return pageQueryService.getHome();
	}

	@GetMapping("/about")
	public PageResponse getAbout() {
		return pageQueryService.getAbout();
	}

	@GetMapping("/project")
	public PageResponse getProject() {
		return pageQueryService.getProject();
	}
}
