package com.myhome.backend.domain.page.dto;

import java.util.List;

public final class FrontendPageResponses {

	private FrontendPageResponses() {
	}

	public record CtaLinkResponse(String label, String href, String variant, Boolean external) {
	}

	public record ContactLinkResponse(String label, String href, String description, Boolean external) {
	}

	public record ContactResponse(
			String email,
			String githubUrl,
			String blogUrl,
			List<ContactLinkResponse> contactLinks
	) {
	}

	public record HomeResponse(
			HomeHeroResponse hero,
			HomeOverviewResponse overview
	) {
	}

	public record HomeHeroResponse(
			String title,
			String headline,
			String description,
			List<CtaLinkResponse> ctaLinks,
			List<HighlightResponse> highlights,
			HomeOperationsResponse operations
	) {
	}

	public record HighlightResponse(String label, String value) {
	}

	public record HomeOperationsResponse(
			String eyebrow,
			String title,
			String status,
			List<HomeServerSnapshotResponse> servers,
			String extensionEyebrow,
			String extensionDescription
	) {
	}

	public record HomeServerSnapshotResponse(
			String id,
			String name,
			String provider,
			String role,
			String status,
			Long latencyMs
	) {
	}

	public record HomeOverviewResponse(
			String eyebrow,
			String title,
			String description,
			OverviewContentResponse featuredProject,
			OverviewContentResponse latestNote,
			OverviewContentResponse currentExperiment,
			List<NavigationCardResponse> navigationCards
	) {
	}

	public record OverviewContentResponse(
			String id,
			String title,
			String description,
			String href,
			List<String> tags
	) {
	}

	public record NavigationCardResponse(String label, String href, String description) {
	}

	public record AboutResponse(
			AboutProfileResponse profile,
			List<SkillCategoryResponse> skills,
			List<ExperienceResponse> experiences,
			ContactResponse contact
	) {
	}

	public record AboutProfileResponse(
			String intro,
			String highlight,
			String secondaryDescription,
			List<StrengthResponse> strengths
	) {
	}

	public record StrengthResponse(String title, String description) {
	}

	public record SkillCategoryResponse(
			String id,
			String title,
			String description,
			List<SkillItemResponse> items
	) {
	}

	public record SkillItemResponse(String name, String level, List<String> tags) {
	}

	public record ExperienceResponse(
			String id,
			String title,
			String description,
			String period,
			String category,
			List<String> tags
	) {
	}

	public record ServicePageResponse(
			List<com.myhome.backend.domain.monitoring.dto.ServiceResponse> services,
			List<ContentItemResponse> mediaItems,
			ContactResponse contact
	) {
	}

	public record ProjectPageResponse(
			List<ProjectItemResponse> projects,
			List<LabFeatureResponse> labFeatures
	) {
	}

	public record ProjectItemResponse(
			String id,
			String name,
			String description,
			List<String> techStack,
			List<String> outcomes,
			List<ProjectLinkResponse> links,
			Boolean featured
	) {
	}

	public record ProjectLinkResponse(String label, String href) {
	}

	public record LabFeatureResponse(
			String id,
			String name,
			String description,
			String status,
			List<String> tags,
			String link
	) {
	}

	public record ContentItemResponse(
			String id,
			String title,
			String description,
			String type,
			String url,
			String publishedAt,
			String source,
			List<String> tags,
			Boolean featured
	) {
	}
}
