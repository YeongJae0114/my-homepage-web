package com.myhome.backend.domain.portfolio.service;

/** A ledger change that would leave the books inconsistent. Rendered as 400 with the message. */
public class LedgerValidationException extends RuntimeException {

	public LedgerValidationException(String message) {
		super(message);
	}
}
