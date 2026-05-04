package com.myhome.backend.domain.page.service;

import com.myhome.backend.domain.page.dto.FrontendPageResponses.AboutResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.HomeResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ProjectPageResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ServicePageResponse;

public interface PageQueryService {

	HomeResponse getHome();

	AboutResponse getAbout();

	ProjectPageResponse getProject();

	ServicePageResponse getService();
}
