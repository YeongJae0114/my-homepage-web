package com.myhome.backend.domain.page.dto;

import java.util.List;

public record ProjectResponse(
		String title,
		String summary,
		String description,
		List<String> techStacks,
		String repositoryUrl,
		String serviceUrl,
		Integer displayOrder
) {
}
