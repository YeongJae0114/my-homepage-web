package com.myhome.backend.domain.page.dto;

public record HeroResponse(
		String headline,
		String subHeadline,
		String description,
		String primaryActionLabel,
		String primaryActionUrl
) {
}
