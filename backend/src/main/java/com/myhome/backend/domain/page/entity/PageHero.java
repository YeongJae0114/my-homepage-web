package com.myhome.backend.domain.page.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "page_heroes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageHero {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "page_id", nullable = false, unique = true)
	private PageContent page;

	@Column(nullable = false, length = 160)
	private String headline;

	@Column(length = 200)
	private String subHeadline;

	@Column(columnDefinition = "text")
	private String description;

	@Column(length = 80)
	private String primaryActionLabel;

	@Column(length = 500)
	private String primaryActionUrl;

	@Builder
	private PageHero(
			PageContent page,
			String headline,
			String subHeadline,
			String description,
			String primaryActionLabel,
			String primaryActionUrl
	) {
		this.page = page;
		this.headline = headline;
		this.subHeadline = subHeadline;
		this.description = description;
		this.primaryActionLabel = primaryActionLabel;
		this.primaryActionUrl = primaryActionUrl;
	}
}
