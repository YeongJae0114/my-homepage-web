package com.myhome.backend.domain.portfolio.service;

import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransactionRequest;
import com.myhome.backend.domain.portfolio.dto.TransactionDtos.TransferRequest;
import com.myhome.backend.domain.portfolio.entity.Account;
import com.myhome.backend.domain.portfolio.entity.Instrument;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction;
import com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.TRANSFER_IN;
import static com.myhome.backend.domain.portfolio.entity.LedgerTransaction.TransactionType.TRANSFER_OUT;

final class TransactionCsv {

	private static final char BOM = '\uFEFF';
	private static final DateTimeFormatter DATE_FORMAT =
			DateTimeFormatter.ofPattern("uuuu-M-d").withResolverStyle(ResolverStyle.STRICT);
	private static final Pattern DECIMAL =
			Pattern.compile("(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]{1,2})?");
	private static final Map<TransactionType, String> LABELS = Map.ofEntries(
			Map.entry(TransactionType.OPENING, "기초잔고"),
			Map.entry(TransactionType.DEPOSIT, "입금"),
			Map.entry(TransactionType.WITHDRAW, "출금"),
			Map.entry(TransactionType.BUY, "매수"),
			Map.entry(TransactionType.SELL, "매도"),
			Map.entry(TransactionType.DIVIDEND, "분배금·배당"),
			Map.entry(TransactionType.INTEREST, "이자"),
			Map.entry(TransactionType.FEE, "수수료"),
			Map.entry(TransactionType.TAX, "세금"),
			Map.entry(TransactionType.LOAN_DRAW, "대출 실행"),
			Map.entry(TransactionType.LOAN_REPAY, "원금 상환"),
			Map.entry(TRANSFER_OUT, "이체"));

	private enum Column {
		DATE("거래일"), ACCOUNT("계좌"), TYPE("유형"), INSTRUMENT("종목"), QUANTITY("수량"), PRICE("단가"),
		FX_RATE("체결환율"), AMOUNT("정산금액"), FEE("수수료"), TAX("세금"), TO_ACCOUNT("입금계좌"), MEMO("메모");

		final String title;

		Column(String title) {
			this.title = title;
		}
	}

	private static final List<String> HEADER = Arrays.stream(Column.values()).map(column -> column.title).toList();

	private TransactionCsv() {
	}

	record Entry(LedgerTransaction trade, LedgerTransaction incoming) {

		List<LedgerTransaction> legs() {
			return incoming == null ? List.of(trade) : List.of(trade, incoming);
		}

		List<String> key() {
			return cells(trade.getAccountId().toString(), toAccountId(), text(trade.getInstrumentId()));
		}

		String label() {
			return LABELS.get(trade.getType());
		}

		private String toAccountId() {
			return incoming == null ? "" : incoming.getAccountId().toString();
		}

		private List<String> cells(String account, String toAccount, String instrument) {
			return List.of(
					trade.getTradeDate().toString(), account, label(), instrument,
					number(trade.getQuantity()), number(trade.getPrice()),
					trade.getQuantity() == null ? "" : number(trade.getFxRate()),
					number(trade.getAmount()), optionalNumber(trade.getFee()), optionalNumber(trade.getTax()),
					toAccount, escapeMemo(text(trade.getMemo())));
		}
	}

	record Row(int line, List<String> cells) {

		boolean transfer() {
			String type = value(Column.TYPE);
			return "이체".equals(type) || "TRANSFER".equalsIgnoreCase(type);
		}

		TransferRequest transferRequest(Map<Long, Account> accounts) {
			requireEmpty(Column.INSTRUMENT, Column.QUANTITY, Column.PRICE, Column.FX_RATE, Column.FEE, Column.TAX);
			BigDecimal amount = decimal(Column.AMOUNT, 17, 0, false);
			if (amount == null) {
				throw error("이체 금액을 입력하세요");
			}
			return new TransferRequest(accountId(Column.ACCOUNT, accounts), accountId(Column.TO_ACCOUNT, accounts),
					date(), amount, memo());
		}

		TransactionRequest transactionRequest(Map<Long, Account> accounts, Map<String, Instrument> instruments) {
			requireEmpty(Column.TO_ACCOUNT);
			TransactionType type = type();
			String code = value(Column.INSTRUMENT).toUpperCase(Locale.ROOT);
			Instrument instrument = instruments.get(code);
			if (!code.isEmpty() && instrument == null) {
				throw error("등록된 종목을 입력하세요 (예: KRX/069500, NASDAQ/AAPL)");
			}
			boolean priced = type == TransactionType.BUY || type == TransactionType.SELL
					|| (type == TransactionType.OPENING && instrument != null);
			if (!priced) {
				requireEmpty(Column.QUANTITY, Column.PRICE, Column.FX_RATE);
			}
			BigDecimal fxRate = decimal(Column.FX_RATE, 13, 6, false);
			if (instrument != null && !instrument.isForeignCurrency()
					&& fxRate != null && fxRate.compareTo(BigDecimal.ONE) != 0) {
				throw error("원화 종목의 체결환율은 비우거나 1을 입력하세요");
			}
			return new TransactionRequest(accountId(Column.ACCOUNT, accounts), type, date(),
					instrument == null ? null : instrument.getId(),
					decimal(Column.QUANTITY, 20, 8, false), decimal(Column.PRICE, 15, 4, false), fxRate,
					decimal(Column.AMOUNT, 17, 0, true), decimal(Column.FEE, 17, 0, true),
					decimal(Column.TAX, 17, 0, true), memo(), false);
		}

		LedgerValidationException error(String message) {
			return new LedgerValidationException("CSV %d행: %s".formatted(line, message));
		}

		private String value(Column column) {
			return cells.get(column.ordinal()).strip();
		}

		private TransactionType type() {
			String value = value(Column.TYPE);
			return LABELS.entrySet().stream()
					.filter(entry -> entry.getKey() != TRANSFER_OUT)
					.filter(entry -> entry.getValue().equals(value) || entry.getKey().name().equalsIgnoreCase(value))
					.map(Map.Entry::getKey)
					.findFirst().orElseThrow(() -> error("거래 유형을 확인하세요"));
		}

		private LocalDate date() {
			try {
				return LocalDate.parse(value(Column.DATE).replace('/', '-').replace('.', '-'), DATE_FORMAT);
			} catch (DateTimeParseException exception) {
				throw error("거래일을 연-월-일 형식으로 입력하세요 (예: 2026-01-15)");
			}
		}

		private Long accountId(Column column, Map<Long, Account> accounts) {
			String[] parts = value(column).split(" - ", 2);
			Account account;
			try {
				account = accounts.get(Long.parseLong(parts[0]));
			} catch (NumberFormatException exception) {
				throw error(column.title + "에 등록된 계좌 ID를 입력하세요");
			}
			if (account == null || (parts.length == 2 && !parts[1].strip().equals(account.getName().strip()))) {
				throw error(column.title + "에 입력한 ID와 이름을 계좌 목록에서 확인하세요");
			}
			return account.getId();
		}

		private BigDecimal decimal(Column column, int integerDigits, int scale, boolean allowZero) {
			String value = value(column);
			if (value.isEmpty()) {
				return null;
			}
			if (value.startsWith("'")) {
				value = value.substring(1);
			}
			if (value.length() > 64 || !DECIMAL.matcher(value).matches()) {
				throw error(column.title + "에 숫자를 입력하세요");
			}
			BigDecimal number = new BigDecimal(value.replace(",", "")).stripTrailingZeros();
			if (number.scale() > scale || number.precision() - number.scale() > integerDigits
					|| (!allowZero && number.signum() == 0)) {
				throw error(column.title + "의 값 또는 소수 자릿수를 확인하세요");
			}
			return number;
		}

		private String memo() {
			String value = unescapeMemo(cells.get(Column.MEMO.ordinal()));
			if (value.length() > 500 || value.indexOf('\0') >= 0) {
				throw error("메모는 500자 이내의 텍스트로 입력하세요");
			}
			return value.isEmpty() ? null : value;
		}

		private void requireEmpty(Column... columns) {
			for (Column column : columns) {
				if (!value(column).isEmpty()) {
					throw error("이 유형에서는 " + column.title + " 칸을 비워 두세요");
				}
			}
		}
	}

	static String instrumentCode(Instrument instrument) {
		return instrument.getMarket().name() + "/" + instrument.getSymbol();
	}

	static List<Entry> entries(List<LedgerTransaction> transactions) {
		Map<UUID, LedgerTransaction> incoming = new HashMap<>();
		for (LedgerTransaction trade : transactions) {
			if (trade.getType() == TRANSFER_IN
					&& (trade.getTransferGroupId() == null || incoming.putIfAbsent(trade.getTransferGroupId(), trade) != null)) {
				throw invalidTransfer();
			}
		}
		List<Entry> entries = new ArrayList<>();
		for (LedgerTransaction trade : transactions) {
			if (trade.getType() == TRANSFER_IN) {
				continue;
			}
			LedgerTransaction paired = trade.getType() == TRANSFER_OUT ? incoming.remove(trade.getTransferGroupId()) : null;
			if (trade.getType() == TRANSFER_OUT && paired == null) {
				throw invalidTransfer();
			}
			entries.add(new Entry(trade, paired));
		}
		if (!incoming.isEmpty()) {
			throw invalidTransfer();
		}
		return entries;
	}

	private static LedgerValidationException invalidTransfer() {
		return new LedgerValidationException("원장에 입금·출금 짝이 맞지 않는 이체가 있습니다");
	}

	static String write(List<Entry> entries, Map<Long, Account> accounts, Map<Long, Instrument> instruments) {
		StringBuilder csv = new StringBuilder().append(BOM);
		appendRow(csv, HEADER);
		for (Entry entry : entries) {
			LedgerTransaction trade = entry.trade();
			appendRow(csv, entry.cells(accountLabel(accounts.get(trade.getAccountId())),
					entry.incoming() == null ? "" : accountLabel(accounts.get(entry.incoming().getAccountId())),
					trade.getInstrumentId() == null ? "" : instrumentCode(instruments.get(trade.getInstrumentId()))));
		}
		return csv.toString();
	}

	private static String accountLabel(Account account) {
		return account.getId() + " - " + account.getName();
	}

	private static void appendRow(StringBuilder csv, List<String> cells) {
		for (int index = 0; index < cells.size(); index++) {
			if (index > 0) {
				csv.append(',');
			}
			String value = cells.get(index);
			if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
				csv.append('"').append(value.replace("\"", "\"\"")).append('"');
			} else {
				csv.append(value);
			}
		}
		csv.append("\r\n");
	}

	private static String number(BigDecimal value) {
		if (value == null) {
			return "";
		}
		BigDecimal normalized = value.stripTrailingZeros();
		String text = normalized.toPlainString();
		// Keep high-precision values as text when opened in spreadsheet apps.
		return normalized.precision() > 15 ? "'" + text : text;
	}

	private static String optionalNumber(BigDecimal value) {
		return value == null || value.signum() == 0 ? "" : number(value);
	}

	private static String text(Object value) {
		return value == null ? "" : value.toString();
	}

	/** Keep memo cells as text in spreadsheets and undo the escaping when importing our export. */
	private static boolean needsMemoEscape(String value) {
		String trimmed = value.stripLeading();
		return value.startsWith("'") || value.startsWith("\t") || value.startsWith("\r") || value.startsWith("\n")
				|| (!trimmed.isEmpty() && "=+-@＝＋－＠".indexOf(trimmed.charAt(0)) >= 0);
	}

	private static String escapeMemo(String value) {
		return needsMemoEscape(value) ? "'" + value : value;
	}

	private static String unescapeMemo(String value) {
		return value.startsWith("'") && needsMemoEscape(value.substring(1)) ? value.substring(1) : value;
	}

	private enum State { START, TEXT, QUOTED, CLOSED }

	/** Reads comma-separated fields, doubled quotes and embedded newlines with physical line numbers. */
	static List<Row> read(String csv) {
		List<Row> rows = new ArrayList<>();
		List<String> cells = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		State state = State.START;
		int line = 1;
		int rowStart = 1;
		for (int i = !csv.isEmpty() && csv.charAt(0) == BOM ? 1 : 0; i < csv.length(); i++) {
			char ch = csv.charAt(i);
			if (ch == '\r' || ch == '\n') {
				boolean crlf = ch == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n';
				line++;
				if (state == State.QUOTED) {
					field.append(crlf ? "\r\n" : String.valueOf(ch));
				} else {
					cells.add(field.toString());
					addRow(rows, cells, rowStart);
					cells = new ArrayList<>();
					field.setLength(0);
					state = State.START;
					rowStart = line;
				}
				if (crlf) {
					i++;
				}
			} else if (ch == ',' && state != State.QUOTED) {
				if (cells.size() >= HEADER.size() - 1) {
					throw new Row(rowStart, List.of()).error("열 개수가 양식과 다릅니다");
				}
				cells.add(field.toString());
				field.setLength(0);
				state = State.START;
			} else if (ch == '"' && state == State.START) {
				state = State.QUOTED;
			} else if (ch == '"' && state == State.QUOTED) {
				if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
					field.append('"');
					i++;
				} else {
					state = State.CLOSED;
				}
			} else if (state == State.CLOSED || ch == '"') {
				throw new Row(rowStart, List.of()).error("큰따옴표 형식을 확인하세요");
			} else {
				field.append(ch);
				state = state == State.QUOTED ? State.QUOTED : State.TEXT;
			}
		}
		if (state == State.QUOTED) {
			throw new Row(rowStart, List.of()).error("닫히지 않은 큰따옴표가 있습니다");
		}
		cells.add(field.toString());
		addRow(rows, cells, rowStart);
		if (rows.isEmpty() || !rows.get(0).cells().stream().map(String::strip).toList().equals(HEADER)) {
			throw new LedgerValidationException("CSV 열 제목과 순서가 양식과 다릅니다. 양식을 내려받아 사용하세요");
		}
		return rows.subList(1, rows.size());
	}

	private static void addRow(List<Row> rows, List<String> cells, int line) {
		if (cells.stream().allMatch(String::isBlank)) {
			return;
		}
		Row row = new Row(line, cells);
		if (cells.size() != HEADER.size()) {
			throw row.error("열 개수가 양식과 다릅니다");
		}
		rows.add(row);
	}
}
