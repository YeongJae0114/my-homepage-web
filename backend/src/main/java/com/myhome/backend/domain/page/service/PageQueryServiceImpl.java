package com.myhome.backend.domain.page.service;

import com.myhome.backend.domain.page.dto.ExperienceResponse;
import com.myhome.backend.domain.page.dto.HeroResponse;
import com.myhome.backend.domain.page.dto.PageResponse;
import com.myhome.backend.domain.page.dto.PageSectionResponse;
import com.myhome.backend.domain.page.dto.ProjectResponse;
import com.myhome.backend.domain.page.dto.SkillResponse;
import com.myhome.backend.domain.page.entity.PageContent;
import com.myhome.backend.domain.page.entity.PageExperience;
import com.myhome.backend.domain.page.entity.PageHero;
import com.myhome.backend.domain.page.entity.PageSection;
import com.myhome.backend.domain.page.entity.PageSkill;
import com.myhome.backend.domain.page.entity.PageType;
import com.myhome.backend.domain.page.entity.ProjectItem;
import com.myhome.backend.domain.page.repository.PageContentRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageQueryServiceImpl implements PageQueryService {

	private final PageContentRepository pageContentRepository;

	@Override
	public PageResponse getHome() {
		return getPage(PageType.HOME);
	}

	@Override
	public PageResponse getAbout() {
		return getPage(PageType.ABOUT);
	}

	@Override
	public PageResponse getProject() {
		return getPage(PageType.PROJECT);
	}

	private PageResponse getPage(PageType type) {
		// TODO: DB 연동 및 비즈니스 로직 구현 - 초기 데이터 적재 전략 확정 후 없는 페이지 처리 정책 보강
		return pageContentRepository.findByType(type)
				.map(this::toResponse)
				.orElseGet(() -> emptyResponse(type));
	}

	private PageResponse toResponse(PageContent page) {
		return new PageResponse(
				page.getType().name().toLowerCase(),
				page.getTitle(),
				page.getSubtitle(),
				page.getDescription(),
				toHeroResponse(page.getHero()),
				toSectionResponses(page.getSections()),
				toSkillResponses(page.getSkills()),
				toExperienceResponses(page.getExperiences()),
				toProjectResponses(page.getProjects())
		);
	}

	private HeroResponse toHeroResponse(PageHero hero) {
		if (hero == null) {
			return null;
		}
		return new HeroResponse(
				hero.getHeadline(),
				hero.getSubHeadline(),
				hero.getDescription(),
				hero.getPrimaryActionLabel(),
				hero.getPrimaryActionUrl()
		);
	}

	private List<PageSectionResponse> toSectionResponses(List<PageSection> sections) {
		return sections.stream()
				.sorted(Comparator.comparing(PageSection::getDisplayOrder))
				.map(section -> new PageSectionResponse(
						section.getKey(),
						section.getTitle(),
						section.getDescription(),
						section.getDisplayOrder(),
						List.copyOf(section.getItems())
				))
				.toList();
	}

	private List<SkillResponse> toSkillResponses(List<PageSkill> skills) {
		return skills.stream()
				.sorted(Comparator.comparing(PageSkill::getDisplayOrder))
				.map(skill -> new SkillResponse(
						skill.getName(),
						skill.getCategory(),
						skill.getLevel(),
						skill.getDisplayOrder()
				))
				.toList();
	}

	private List<ExperienceResponse> toExperienceResponses(List<PageExperience> experiences) {
		return experiences.stream()
				.sorted(Comparator.comparing(PageExperience::getDisplayOrder))
				.map(experience -> new ExperienceResponse(
						experience.getCompany(),
						experience.getRole(),
						experience.getPeriod(),
						experience.getDescription(),
						experience.getDisplayOrder(),
						List.copyOf(experience.getHighlights())
				))
				.toList();
	}

	private List<ProjectResponse> toProjectResponses(List<ProjectItem> projects) {
		return projects.stream()
				.sorted(Comparator.comparing(ProjectItem::getDisplayOrder))
				.map(project -> new ProjectResponse(
						project.getTitle(),
						project.getSummary(),
						project.getDescription(),
						List.copyOf(project.getTechStacks()),
						project.getRepositoryUrl(),
						project.getServiceUrl(),
						project.getDisplayOrder()
				))
				.toList();
	}

	private PageResponse emptyResponse(PageType type) {
		return new PageResponse(
				type.name().toLowerCase(),
				null,
				null,
				null,
				null,
				List.of(),
				List.of(),
				List.of(),
				List.of()
		);
	}
}
