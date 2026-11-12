package com.myhome.backend.domain.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public final class TransactionCsvDtos {

	public static final int MAX_CSV_LENGTH = 5 * 1024 * 1024;

	private TransactionCsvDtos() {
	}

	public record CsvFile(String filename, String content) {
	}

	public record CsvRequest(
			@NotBlank(message = "CSV 내용을 입력하세요")
			@Size(max = MAX_CSV_LENGTH, message = "CSV가 너무 큽니다. 파일을 나눠서 가져오세요")
			String content
	) {
	}

	public record ImportRow(
			int line, LocalDate tradeDate, String account, String type, String instrument,
			String amount, String toAccount, boolean duplicate
	) {
	}

	public record ImportResult(int newRows, int duplicateRows, List<ImportRow> rows) {
	}
}
