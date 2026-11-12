package com.myhome.backend.domain.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.CsvRequest;
import com.myhome.backend.domain.portfolio.dto.TransactionCsvDtos.ImportResult;
import com.myhome.backend.domain.portfolio.service.LedgerService;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url="
		+ "jdbc:h2:mem:portfolio_csv_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@WithMockUser(roles = "OWNER")
class TransactionCsvApiTest {

	private static final String BASE = "/api/portfolio/transactions";
	private static final String CSV = BASE + "/csv";
	private static final JsonMapper JSON = JsonMapper.builder().build();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LedgerService ledgerService;

	@Test
	void downloadsTemplateAndRoundTripsKoreanMemoInstrumentCodeAndTransfer() throws Exception {
		long bank = account("BANK");
		long brokerage = account("BROKERAGE");
		postJson("/api/portfolio/instruments", """
				{"symbol":"069500","name":"가상 ETF","market":"KRX","instrumentType":"ETF",
				 "underlyingAssetClass":"EQUITY","exposureRegion":"KR","fxHedged":false,"taxType":"DOMESTIC_EQUITY_ETF"}
				""").andExpect(status().isCreated());

		String template = download("template");
		assertThat(template).startsWith("\uFEFF거래일,계좌,유형,종목,").endsWith(",메모\r\n");
		String memo = "=기록, \"가상\"\r\n다음 줄";
		String csv = template
				+ row("2026-01-15", "" + bank, "입금", "", "", "", "", "1,000,000", "", "", "", "")
				+ row("2026-01-15", "" + bank, "이체", "", "", "", "", "100000", "", "", "" + brokerage, memo)
				+ row("2026-01-15", "" + brokerage, "매수", "KRX/069500", "2", "10000", "", "", "30", "", "", "");

		upload("preview", csv).andExpect(status().isOk())
				.andExpect(jsonPath("$.newRows").value(3))
				.andExpect(jsonPath("$.rows[1].toAccount").value("CSV 가상 BROKERAGE"))
				.andExpect(jsonPath("$.rows[2].line").value(5))
				.andExpect(jsonPath("$.rows[2].amount").value("20030"));
		assertCount(bank, 0);
		upload("import", csv).andExpect(status().isOk()).andExpect(jsonPath("$.newRows").value(3));
		assertCount(bank, 2);
		assertCount(brokerage, 2);
		mockMvc.perform(get(BASE).param("accountId", "" + bank))
				.andExpect(jsonPath("$.items[0].memo").value(memo));

		String exported = download("export");
		assertThat(exported).contains("KRX/069500", "\"'=기록, \"\"가상\"\"\r\n다음 줄\"")
				.containsOnlyOnce(",이체,");
		upload("import", exported).andExpect(status().isOk())
				.andExpect(jsonPath("$.newRows").value(0));
		assertCount(bank, 2);
		assertCount(brokerage, 2);
	}

	@Test
	void rejectsOversellingAndMalformedRowsWithoutPartialWrites() throws Exception {
		long account = account("ISA");
		postJson("/api/portfolio/instruments", """
				{"symbol":"012345","name":"가상 주식","market":"KRX","instrumentType":"STOCK",
				 "underlyingAssetClass":"EQUITY","exposureRegion":"KR","fxHedged":false,"taxType":"DOMESTIC_STOCK"}
				""").andExpect(status().isCreated());
		String deposit = download("template").replace("\uFEFF", "").replace("\r\n", "\n")
				+ row("2026-01-15", "" + account, "입금", "", "", "", "", "10000", "", "", "", "");
		String sell = row("2026-01-15", "" + account, "매도", "KRX/012345", "1", "10000", "", "", "", "", "", "");
		upload("import", deposit + sell).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("매도 수량이 보유 수량을 초과")));
		upload("import", deposit + "\"닫히지 않은 메모").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("CSV 3행")));
		assertCount(account, 0);
	}

	@Test
	void exportsLargeAmountsExactlyAndRejectsFractionalWon() throws Exception {
		long account = account("BANK");
		String template = download("template");
		String amount = "12345678901234567";
		String deposit = row("2026-01-15", "" + account, "입금", "", "", "", "", amount, "", "", "", "");
		upload("import", template + deposit).andExpect(status().isOk())
				.andExpect(jsonPath("$.rows[0].amount").value(amount));
		String exported = download("export");
		assertThat(exported).contains(",'" + amount + ",");
		upload("import", exported).andExpect(status().isOk()).andExpect(jsonPath("$.newRows").value(0));
		String fractional = row("2026-01-15", "" + account, "입금", "", "", "", "", "1000.1", "", "", "", "");
		upload("import", template + fractional).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("정산금액")));
		assertCount(account, 1);
	}

	@Test
	void retainsMultiplicityAndSerializesConcurrentImports() throws Exception {
		long account = account("BANK");
		String template = download("template");
		String deposit = row("2026-01-15", "" + account, "입금", "", "", "", "", "10000", "", "", "", "");
		CsvRequest request = new CsvRequest(template + deposit + deposit);
		var first = CompletableFuture.supplyAsync(() -> ledgerService.importTransactionsCsv(request));
		var second = CompletableFuture.supplyAsync(() -> ledgerService.importTransactionsCsv(request));
		ImportResult firstResult = first.get(10, TimeUnit.SECONDS);
		ImportResult secondResult = second.get(10, TimeUnit.SECONDS);

		assertThat(firstResult.newRows() + secondResult.newRows()).isEqualTo(2);
		assertThat(firstResult.duplicateRows() + secondResult.duplicateRows()).isEqualTo(2);
		assertCount(account, 2);
	}

	@Test
	void requiresOwnerAndCsrf() throws Exception {
		mockMvc.perform(get(CSV + "/export").with(anonymous())).andExpect(status().isUnauthorized());
		mockMvc.perform(post(CSV + "/import").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isForbidden());
	}

	private long account(String type) throws Exception {
		var response = postJson("/api/portfolio/accounts", """
				{"name":"CSV 가상 %s","institution":"가상증권","accountType":"%s"}
				""".formatted(type, type)).andExpect(status().isCreated()).andReturn().getResponse();
		return ((Number) JsonPath.read(response.getContentAsString(StandardCharsets.UTF_8), "$.id")).longValue();
	}

	private String download(String action) throws Exception {
		var response = mockMvc.perform(get(CSV + "/" + action)).andExpect(status().isOk())
				.andReturn().getResponse();
		return JsonPath.read(response.getContentAsString(StandardCharsets.UTF_8), "$.content");
	}

	private ResultActions upload(String action, String content) throws Exception {
		return postJson(CSV + "/" + action, JSON.writeValueAsString(Map.of("content", content)));
	}

	private ResultActions postJson(String path, String json) throws Exception {
		return mockMvc.perform(post(path).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private void assertCount(long account, int count) throws Exception {
		mockMvc.perform(get(BASE).param("accountId", "" + account))
				.andExpect(jsonPath("$.totalElements").value(count));
	}

	private static String row(String... cells) {
		return "\"" + String.join("\",\"", java.util.Arrays.stream(cells)
				.map(cell -> cell.replace("\"", "\"\"")).toList()) + "\"\r\n";
	}
}
