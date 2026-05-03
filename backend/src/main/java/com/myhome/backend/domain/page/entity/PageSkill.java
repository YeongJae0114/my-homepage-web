package com.myhome.backend.domain.page.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "page_skills")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageSkill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "page_id", nullable = false)
	private PageContent page;

	@Column(nullable = false, length = 80)
	private String name;

	@Column(nullable = false, length = 80)
	private String category;

	@Column(length = 40)
	private String level;

	@Column(nullable = false)
	private Integer displayOrder;

	@Builder
	private PageSkill(PageContent page, String name, String category, String level, Integer displayOrder) {
		this.page = page;
		this.name = name;
		this.category = category;
		this.level = level;
		this.displayOrder = displayOrder;
	}
}
