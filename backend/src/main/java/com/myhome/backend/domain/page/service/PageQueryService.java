package com.myhome.backend.domain.page.service;

import com.myhome.backend.domain.page.dto.PageResponse;

public interface PageQueryService {

	PageResponse getHome();

	PageResponse getAbout();

	PageResponse getProject();
}
