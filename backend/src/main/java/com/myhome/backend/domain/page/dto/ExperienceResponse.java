package com.myhome.backend.domain.page.dto;

import java.util.List;

public record ExperienceResponse(
		String company,
		String role,
		String period,
		String description,
		Integer displayOrder,
		List<String> highlights
) {
}
