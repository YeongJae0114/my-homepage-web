package com.myhome.backend.domain.page.dto;

import java.util.List;

public record PageResponse(
		String page,
		String title,
		String subtitle,
		String description,
		HeroResponse hero,
		List<PageSectionResponse> sections,
		List<SkillResponse> skills,
		List<ExperienceResponse> experiences,
		List<ProjectResponse> projects
) {
}
