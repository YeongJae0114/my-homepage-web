package com.myhome.backend.domain.page.repository;

import com.myhome.backend.domain.page.entity.PageSkill;
import com.myhome.backend.domain.page.entity.PageType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageSkillRepository extends JpaRepository<PageSkill, Long> {

	List<PageSkill> findByPageTypeOrderByDisplayOrderAsc(PageType type);
}
