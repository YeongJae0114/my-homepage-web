package com.myhome.backend.domain.portfolio;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Each test creates its own accounts and symbols: the H2 database is shared across test contexts. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "OWNER")
class LedgerApiTest {

	private static final AtomicInteger SYMBOL_SEQUENCE = new AtomicInteger(100000);

	@Autowired
	private MockMvc mockMvc;

	@Test
	void ledgerDerivesHoldingsRealizedProfitAndCash() throws Exception {
		long account = createAccount("BROKERAGE");
		long etf = createKrxEtf();

		postJson("/api/portfolio/transactions", transaction(account, "OPENING", null, null, null, "1000000", null))
				.andExpect(status().isCreated());
		// amount omitted: 10 x 10,000 + fee 15
		postJson("/api/portfolio/transactions", transaction(account, "BUY", etf, "10", "10000", null, "\"fee\":15"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.amount").value(100015));
		// 4 x 11,000 - fee 10 - tax 80
		postJson("/api/portfolio/transactions", transaction(account, "SELL", etf, "4", "11000", null, "\"fee\":10,\"tax\":80"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.amount").value(43910));

		mockMvc.perform(get("/api/portfolio/holdings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.positions[?(@.accountId == %d)].quantity".formatted(account)).value(6))
				.andExpect(jsonPath("$.positions[?(@.accountId == %d)].costKrw".formatted(account)).value(60009))
				.andExpect(jsonPath("$.positions[?(@.accountId == %d)].realizedKrw".formatted(account)).value(3904))
				.andExpect(jsonPath("$.balances[?(@.accountId == %d)].cashKrw".formatted(account)).value(943895));
	}

	@Test
	void oversellingAndBreakingDeletesAreRejected() throws Exception {
		long account = createAccount("ISA");
		long etf = createKrxEtf();
		long buyId = idOf(postJson("/api/portfolio/transactions", transaction(account, "BUY", etf, "5", "10000", null, null))
				.andExpect(status().isCreated()));
		postJson("/api/portfolio/transactions", transaction(account, "SELL", etf, "5", "10000", null, null))
				.andExpect(status().isCreated());

		postJson("/api/portfolio/transactions", transaction(account, "SELL", etf, "1", "10000", null, null))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("보유 0, 매도 1")));

		// Removing the BUY would leave the later SELL uncovered: rejected and rolled back.
		mockMvc.perform(delete("/api/portfolio/transactions/" + buyId).with(csrf()))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/portfolio/transactions").param("accountId", String.valueOf(account)))
				.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	void typeRulesAreEnforced() throws Exception {
		long bank = createAccount("BANK");
		long brokerage = createAccount("BROKERAGE");
		long etf = createKrxEtf();
		String tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1).toString();

		postJson("/api/portfolio/transactions", transaction(bank, "BUY", etf, "1", "10000", null, null))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("투자 계좌")));
		postJson("/api/portfolio/transactions", transaction(brokerage, "LOAN_DRAW", null, null, null, "1000", null))
				.andExpect(status().isBadRequest());
		postJson("/api/portfolio/transactions", """
				{"accountId":%d,"type":"DEPOSIT","tradeDate":"%s","amount":1000}""".formatted(brokerage, tomorrow))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("미래")));
		postJson("/api/portfolio/transactions", """
				{"accountId":%d,"type":"DEPOSIT"}""".formatted(brokerage))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.tradeDate").exists());
	}

	@Test
	void transferIsAPairAndDeletesTogether() throws Exception {
		long bank = createAccount("BANK");
		long brokerage = createAccount("BROKERAGE");

		ResultActions created = postJson("/api/portfolio/transfers", """
				{"fromAccountId":%d,"toAccountId":%d,"tradeDate":"2026-01-05","amount":500000}"""
				.formatted(bank, brokerage))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$[0].type").value("TRANSFER_OUT"))
				.andExpect(jsonPath("$[1].type").value("TRANSFER_IN"));
		long outLegId = ((Number) JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$[0].id")).longValue();

		mockMvc.perform(delete("/api/portfolio/transactions/" + outLegId).with(csrf()))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/portfolio/transactions").param("accountId", String.valueOf(brokerage)))
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	private long createAccount(String type) throws Exception {
		return idOf(postJson("/api/portfolio/accounts", """
				{"name":"테스트 %s","institution":"테스트증권","accountType":"%s"}""".formatted(type, type))
				.andExpect(status().isCreated()));
	}

	private long createKrxEtf() throws Exception {
		return idOf(postJson("/api/portfolio/instruments", """
				{"symbol":"%d","name":"테스트 ETF","market":"KRX","instrumentType":"ETF",
				 "underlyingAssetClass":"EQUITY","exposureRegion":"KR","fxHedged":false,"taxType":"DOMESTIC_EQUITY_ETF"}"""
				.formatted(SYMBOL_SEQUENCE.incrementAndGet()))
				.andExpect(status().isCreated()));
	}

	private static String transaction(
			long account, String type, Long instrument, String quantity, String price, String amount, String extra) {
		StringBuilder json = new StringBuilder("{\"accountId\":%d,\"type\":\"%s\",\"tradeDate\":\"2026-01-02\""
				.formatted(account, type));
		if (instrument != null) {
			json.append(",\"instrumentId\":").append(instrument);
		}
		if (quantity != null) {
			json.append(",\"quantity\":").append(quantity).append(",\"price\":").append(price);
		}
		if (amount != null) {
			json.append(",\"amount\":").append(amount);
		}
		if (extra != null) {
			json.append(',').append(extra);
		}
		return json.append('}').toString();
	}

	private ResultActions postJson(String path, String body) throws Exception {
		return mockMvc.perform(post(path).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private static long idOf(ResultActions result) throws Exception {
		return ((Number) JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
	}
}
