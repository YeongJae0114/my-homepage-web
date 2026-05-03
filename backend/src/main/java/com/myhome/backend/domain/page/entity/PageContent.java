package com.myhome.backend.domain.page.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "page_contents",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_page_contents_type", columnNames = "page_type")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageContent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "page_type", nullable = false, length = 30)
	private PageType type;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(length = 180)
	private String subtitle;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false)
	private Integer displayOrder;

	@OneToOne(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
	private PageHero hero;

	@OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PageSection> sections = new ArrayList<>();

	@OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PageSkill> skills = new ArrayList<>();

	@OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PageExperience> experiences = new ArrayList<>();

	@OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProjectItem> projects = new ArrayList<>();

	@Builder
	private PageContent(PageType type, String title, String subtitle, String description, Integer displayOrder) {
		this.type = type;
		this.title = title;
		this.subtitle = subtitle;
		this.description = description;
		this.displayOrder = displayOrder;
	}
}
