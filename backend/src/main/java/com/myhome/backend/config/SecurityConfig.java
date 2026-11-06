package com.myhome.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
public class SecurityConfig {

	private static final String OWNER_ROLE = "OWNER";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		PathPatternRequestMatcher.Builder path = PathPatternRequestMatcher.withDefaults();

		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/portfolio/**").hasRole(OWNER_ROLE)
						// Everything that existed before owner auth stays public.
						.anyRequest().permitAll())
				// CSRF only matters where the session cookie grants something: auth and owner APIs.
				// Public POSTs (e.g. /api/llm/chat) carry no privilege and keep working without a token.
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
						.requireCsrfProtectionMatcher(new AndRequestMatcher(
								CsrfFilter.DEFAULT_CSRF_MATCHER,
								new OrRequestMatcher(path.matcher("/api/auth/**"), path.matcher("/api/portfolio/**")))))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(logout -> logout
						.logoutRequestMatcher(path.matcher(HttpMethod.POST, "/api/auth/logout"))
						.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
						.deleteCookies("JSESSIONID"));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	/**
	 * Missing or unsafe credentials disable owner login instead of failing startup,
	 * so the public site keeps running if a deploy forgets the owner secrets.
	 */
	@Bean
	UserDetailsService userDetailsService(OwnerProperties owner, Environment environment) {
		if (!StringUtils.hasText(owner.username()) || !StringUtils.hasText(owner.passwordHash())) {
			log.warn("Owner credentials are not configured; owner login is disabled.");
			return new InMemoryUserDetailsManager();
		}

		String passwordHash = owner.passwordHash().trim();
		if (passwordHash.startsWith("$2")) {
			passwordHash = "{bcrypt}" + passwordHash;
		}
		if (passwordHash.startsWith("{noop}") && !environment.matchesProfiles("local", "test")) {
			log.error("Plain-text owner password is only allowed in local/test profiles; owner login is disabled.");
			return new InMemoryUserDetailsManager();
		}

		return new InMemoryUserDetailsManager(User.withUsername(owner.username().trim())
				.password(passwordHash)
				.roles(OWNER_ROLE)
				.build());
	}

	@Bean
	AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}
}
