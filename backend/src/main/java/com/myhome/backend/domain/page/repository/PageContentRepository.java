package com.myhome.backend.domain.page.repository;

import com.myhome.backend.domain.page.entity.PageContent;
import com.myhome.backend.domain.page.entity.PageType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageContentRepository extends JpaRepository<PageContent, Long> {

	Optional<PageContent> findByType(PageType type);
}
