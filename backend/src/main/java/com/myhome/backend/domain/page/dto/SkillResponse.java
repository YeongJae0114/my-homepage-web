package com.myhome.backend.domain.page.dto;

public record SkillResponse(
		String name,
		String category,
		String level,
		Integer displayOrder
) {
}
