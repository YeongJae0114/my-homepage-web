package com.myhome.backend.domain.page.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "page_experiences")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageExperience {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "page_id", nullable = false)
	private PageContent page;

	@Column(nullable = false, length = 120)
	private String company;

	@Column(nullable = false, length = 120)
	private String role;

	@Column(length = 80)
	private String period;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false)
	private Integer displayOrder;

	@ElementCollection
	@CollectionTable(name = "page_experience_highlights", joinColumns = @JoinColumn(name = "experience_id"))
	@OrderColumn(name = "highlight_order")
	@Column(name = "highlight", nullable = false, length = 500)
	private List<String> highlights = new ArrayList<>();

	@Builder
	private PageExperience(
			PageContent page,
			String company,
			String role,
			String period,
			String description,
			Integer displayOrder,
			List<String> highlights
	) {
		this.page = page;
		this.company = company;
		this.role = role;
		this.period = period;
		this.description = description;
		this.displayOrder = displayOrder;
		this.highlights = highlights == null ? new ArrayList<>() : highlights;
	}
}
