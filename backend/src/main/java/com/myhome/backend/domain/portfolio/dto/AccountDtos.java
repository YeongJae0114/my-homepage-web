package com.myhome.backend.domain.portfolio.dto;

import com.myhome.backend.domain.portfolio.entity.Account;
import com.myhome.backend.domain.portfolio.entity.Account.AccountType;
import com.myhome.backend.domain.portfolio.entity.Account.RepaymentType;
import com.myhome.backend.domain.portfolio.entity.ManualValuation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class AccountDtos {

	private AccountDtos() {
	}

	public record AccountRequest(
			@NotBlank @Size(max = 100) String name,
			@NotBlank @Size(max = 100) String institution,
			@NotNull AccountType accountType,
			@Pattern(regexp = "\\d{4}", message = "끝 4자리 숫자만 입력하세요") String accountNumberLast4,
			@DecimalMin("0") @DecimalMax("100") BigDecimal interestRate,
			@PositiveOrZero BigDecimal monthlyPayment,
			RepaymentType repaymentType,
			LocalDate openedAt,
			LocalDate maturityDate,
			LocalDate closedAt,
			Integer displayOrder,
			@Size(max = 500) String memo
	) {
		public Account toEntity() {
			return Account.builder()
					.name(name.trim())
					.institution(institution.trim())
					.accountType(accountType)
					.accountNumberLast4(accountNumberLast4)
					.interestRate(interestRate)
					.monthlyPayment(monthlyPayment)
					.repaymentType(repaymentType)
					.openedAt(openedAt)
					.maturityDate(maturityDate)
					.closedAt(closedAt)
					.displayOrder(displayOrder == null ? 0 : displayOrder)
					.memo(memo)
					.build();
		}
	}

	public record AccountResponse(
			Long id,
			String name,
			String institution,
			AccountType accountType,
			boolean investment,
			String accountNumberLast4,
			BigDecimal interestRate,
			BigDecimal monthlyPayment,
			RepaymentType repaymentType,
			LocalDate openedAt,
			LocalDate maturityDate,
			LocalDate closedAt,
			int displayOrder,
			String memo
	) {
		public static AccountResponse from(Account account) {
			return new AccountResponse(
					account.getId(),
					account.getName(),
					account.getInstitution(),
					account.getAccountType(),
					account.getAccountType().isInvestment(),
					account.getAccountNumberLast4(),
					account.getInterestRate(),
					PortfolioNumbers.krw(account.getMonthlyPayment()),
					account.getRepaymentType(),
					account.getOpenedAt(),
					account.getMaturityDate(),
					account.getClosedAt(),
					account.getDisplayOrder(),
					account.getMemo());
		}
	}

	public record ValuationRequest(
			@NotNull LocalDate valuationDate,
			@NotNull @PositiveOrZero BigDecimal amount,
			@Size(max = 500) String memo
	) {
	}

	public record ValuationResponse(Long id, Long accountId, LocalDate valuationDate, BigDecimal amount, String memo) {

		public static ValuationResponse from(ManualValuation valuation) {
			return new ValuationResponse(
					valuation.getId(),
					valuation.getAccountId(),
					valuation.getValuationDate(),
					PortfolioNumbers.krw(valuation.getAmount()),
					valuation.getMemo());
		}
	}
}
