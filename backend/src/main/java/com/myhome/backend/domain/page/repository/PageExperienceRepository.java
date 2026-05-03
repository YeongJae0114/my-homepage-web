package com.myhome.backend.domain.page.repository;

import com.myhome.backend.domain.page.entity.PageExperience;
import com.myhome.backend.domain.page.entity.PageType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageExperienceRepository extends JpaRepository<PageExperience, Long> {

	List<PageExperience> findByPageTypeOrderByDisplayOrderAsc(PageType type);
}
