package com.myhome.backend.domain.page.dto;

import java.util.List;

public record PageSectionResponse(
		String key,
		String title,
		String description,
		Integer displayOrder,
		List<String> items
) {
}
