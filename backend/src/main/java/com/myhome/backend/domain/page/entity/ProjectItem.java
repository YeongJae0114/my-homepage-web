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
@Table(name = "project_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "page_id", nullable = false)
	private PageContent page;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(nullable = false, length = 240)
	private String summary;

	@Column(columnDefinition = "text")
	private String description;

	@Column(length = 500)
	private String repositoryUrl;

	@Column(length = 500)
	private String serviceUrl;

	@Column(nullable = false)
	private Integer displayOrder;

	@ElementCollection
	@CollectionTable(name = "project_item_tech_stacks", joinColumns = @JoinColumn(name = "project_id"))
	@OrderColumn(name = "tech_stack_order")
	@Column(name = "tech_stack", nullable = false, length = 80)
	private List<String> techStacks = new ArrayList<>();

	@Builder
	private ProjectItem(
			PageContent page,
			String title,
			String summary,
			String description,
			String repositoryUrl,
			String serviceUrl,
			Integer displayOrder,
			List<String> techStacks
	) {
		this.page = page;
		this.title = title;
		this.summary = summary;
		this.description = description;
		this.repositoryUrl = repositoryUrl;
		this.serviceUrl = serviceUrl;
		this.displayOrder = displayOrder;
		this.techStacks = techStacks == null ? new ArrayList<>() : techStacks;
	}
}
