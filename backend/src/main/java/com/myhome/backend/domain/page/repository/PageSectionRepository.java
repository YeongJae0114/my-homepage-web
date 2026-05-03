package com.myhome.backend.domain.page.repository;

import com.myhome.backend.domain.page.entity.PageSection;
import com.myhome.backend.domain.page.entity.PageType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageSectionRepository extends JpaRepository<PageSection, Long> {

	List<PageSection> findByPageTypeOrderByDisplayOrderAsc(PageType type);
}
