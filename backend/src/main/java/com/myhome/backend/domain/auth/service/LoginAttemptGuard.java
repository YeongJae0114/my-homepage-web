package com.myhome.backend.domain.auth.service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Locks owner login after repeated failures.
 * ponytail: one global counter (requests arrive via the Vercel proxy, so client IPs are not trustworthy).
 * An attacker can keep the owner locked out for 15 minutes at a time; add TOTP and relax the lock if that matters.
 */
@Component
public class LoginAttemptGuard {

	static final int MAX_FAILURES = 5;
	static final Duration LOCK_DURATION = Duration.ofMinutes(15);

	private int failures;
	private Instant lockedUntil = Instant.MIN;

	public synchronized boolean isLocked() {
		return Instant.now().isBefore(lockedUntil);
	}

	public synchronized void recordFailure() {
		failures++;
		if (failures >= MAX_FAILURES) {
			lockedUntil = Instant.now().plus(LOCK_DURATION);
			failures = 0;
		}
	}

	public synchronized void recordSuccess() {
		failures = 0;
	}
}
