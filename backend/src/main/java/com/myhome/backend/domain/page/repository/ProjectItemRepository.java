package com.myhome.backend.domain.page.repository;

import com.myhome.backend.domain.page.entity.PageType;
import com.myhome.backend.domain.page.entity.ProjectItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectItemRepository extends JpaRepository<ProjectItem, Long> {

	List<ProjectItem> findByPageTypeOrderByDisplayOrderAsc(PageType type);
}
