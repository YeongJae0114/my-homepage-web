package com.myhome.backend.domain.auth.controller;

import com.myhome.backend.domain.auth.dto.LoginRequest;
import com.myhome.backend.domain.auth.dto.OwnerStatusResponse;
import com.myhome.backend.domain.auth.service.LoginAttemptGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final LoginAttemptGuard loginAttemptGuard;
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	/** Always 200. Also issues the XSRF-TOKEN cookie the SPA needs before its first POST. */
	@GetMapping("/me")
	public OwnerStatusResponse me(Authentication authentication, CsrfToken csrfToken) {
		csrfToken.getToken();
		boolean owner = authentication != null && authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_OWNER".equals(authority.getAuthority()));
		return new OwnerStatusResponse(owner);
	}

	@PostMapping("/login")
	public ResponseEntity<Void> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		if (loginAttemptGuard.isLocked()) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
		}

		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
		} catch (AuthenticationException exception) {
			loginAttemptGuard.recordFailure();
			// Same response for unknown user and wrong password.
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		loginAttemptGuard.recordSuccess();

		// Session fixation: never keep a session id that existed before login.
		if (httpRequest.getSession(false) != null) {
			httpRequest.changeSessionId();
		}
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, httpRequest, httpResponse);

		return ResponseEntity.noContent().build();
	}
}
