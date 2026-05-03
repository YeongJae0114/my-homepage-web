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
@Table(name = "page_sections")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageSection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "page_id", nullable = false)
	private PageContent page;

	@Column(name = "section_key", nullable = false, length = 60)
	private String key;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false)
	private Integer displayOrder;

	@ElementCollection
	@CollectionTable(name = "page_section_items", joinColumns = @JoinColumn(name = "section_id"))
	@OrderColumn(name = "item_order")
	@Column(name = "item", nullable = false, length = 500)
	private List<String> items = new ArrayList<>();

	@Builder
	private PageSection(PageContent page, String key, String title, String description, Integer displayOrder, List<String> items) {
		this.page = page;
		this.key = key;
		this.title = title;
		this.description = description;
		this.displayOrder = displayOrder;
		this.items = items == null ? new ArrayList<>() : items;
	}
}
