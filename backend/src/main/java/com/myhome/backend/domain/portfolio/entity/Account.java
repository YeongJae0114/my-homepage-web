package com.myhome.backend.domain.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "portfolio_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

	public enum AccountType {
		// Investment accounts: hold securities and form the XIRR/TWR scope.
		BROKERAGE(true), ISA(true), PENSION_SAVINGS(true), IRP(true),
		BANK(false), CMA(false), TIME_DEPOSIT(false), INSTALLMENT_SAVINGS(false),
		INSURANCE(false),
		LOAN(false);

		private final boolean investment;

		AccountType(boolean investment) {
			this.investment = investment;
		}

		public boolean isInvestment() {
			return investment;
		}
	}

	public enum RepaymentType { EQUAL_PAYMENT, EQUAL_PRINCIPAL, BULLET }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 100)
	private String institution;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AccountType accountType;

	@Column(length = 4)
	private String accountNumberLast4;

	@Column(precision = 7, scale = 4)
	private BigDecimal interestRate;

	@Column(precision = 19, scale = 2)
	private BigDecimal monthlyPayment;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private RepaymentType repaymentType;

	private LocalDate openedAt;

	private LocalDate maturityDate;

	private LocalDate closedAt;

	@Column(nullable = false)
	private int displayOrder;

	@Column(length = 500)
	private String memo;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private Account(
			String name,
			String institution,
			AccountType accountType,
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
		this.name = name;
		this.institution = institution;
		this.accountType = accountType;
		this.accountNumberLast4 = accountNumberLast4;
		this.interestRate = interestRate;
		this.monthlyPayment = monthlyPayment;
		this.repaymentType = repaymentType;
		this.openedAt = openedAt;
		this.maturityDate = maturityDate;
		this.closedAt = closedAt;
		this.displayOrder = displayOrder;
		this.memo = memo;
	}

	public void overwriteWith(Account source) {
		this.name = source.name;
		this.institution = source.institution;
		this.accountType = source.accountType;
		this.accountNumberLast4 = source.accountNumberLast4;
		this.interestRate = source.interestRate;
		this.monthlyPayment = source.monthlyPayment;
		this.repaymentType = source.repaymentType;
		this.openedAt = source.openedAt;
		this.maturityDate = source.maturityDate;
		this.closedAt = source.closedAt;
		this.displayOrder = source.displayOrder;
		this.memo = source.memo;
	}
}
