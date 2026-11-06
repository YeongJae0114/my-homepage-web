package com.myhome.backend.domain.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTest {

	private static final String VALID_LOGIN = """
			{"username":"owner","password":"test-password"}""";
	private static final String WRONG_LOGIN = """
			{"username":"owner","password":"wrong"}""";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void ownerApiRejectsAnonymousRequests() throws Exception {
		mockMvc.perform(get("/api/portfolio/dashboard"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void existingPublicApiStaysOpen() throws Exception {
		mockMvc.perform(get("/api/monitoring"))
				.andExpect(status().isOk());
	}

	@Test
	void meReportsAnonymousAndIssuesCsrfCookie() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.owner").value(false))
				.andExpect(cookie().exists("XSRF-TOKEN"));
	}

	@Test
	void loginRequiresCsrfToken() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN))
				.andExpect(status().isForbidden());
	}

	@Test
	void loginGrantsOwnerAccessUntilLogout() throws Exception {
		MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login").with(csrf())
						.contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN))
				.andExpect(status().isNoContent())
				.andReturn().getRequest().getSession(false);

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(jsonPath("$.owner").value(true));
		mockMvc.perform(get("/api/portfolio/dashboard").session(session))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/logout").with(csrf()).session(session))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/portfolio/dashboard").session(session))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DirtiesContext
	void repeatedFailuresLockLoginEvenForCorrectPassword() throws Exception {
		for (int attempt = 0; attempt < 5; attempt++) {
			mockMvc.perform(post("/api/auth/login").with(csrf())
							.contentType(MediaType.APPLICATION_JSON).content(WRONG_LOGIN))
					.andExpect(status().isUnauthorized());
		}

		mockMvc.perform(post("/api/auth/login").with(csrf())
						.contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN))
				.andExpect(status().isTooManyRequests());
	}
}
