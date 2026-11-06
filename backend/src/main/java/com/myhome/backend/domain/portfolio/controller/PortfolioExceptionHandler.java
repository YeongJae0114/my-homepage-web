package com.myhome.backend.domain.portfolio.controller;

import com.myhome.backend.domain.portfolio.service.LedgerValidationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Owner UI needs the reason: render portfolio errors as RFC 7807 problem details with the message. */
@RestControllerAdvice(basePackages = "com.myhome.backend.domain.portfolio")
public class PortfolioExceptionHandler {

	@ExceptionHandler(LedgerValidationException.class)
	public ProblemDetail handleLedgerValidation(LedgerValidationException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ProblemDetail handleStatus(ResponseStatusException exception) {
		return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleInvalidBody(MethodArgumentNotValidException exception) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError error : exception.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "입력값을 확인하세요");
		problem.setProperty("errors", errors);
		return problem;
	}
}
