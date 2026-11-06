package com.myhome.backend.domain.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.myhome.backend.domain.portfolio.repository.PortfolioSnapshotRepository;
import com.myhome.backend.domain.portfolio.scheduler.PortfolioCollectScheduler;
import java.time.LocalDate;
import java.time.ZoneId;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * End-to-end valuation on its own empty database, so totals are exact.
 * Brokerage: 1,000,000 opening cash; 10 KRX ETF @10,000 and 2 US shares @100 USD x 1,300.
 * Priced at ETF 12,000, US 110 USD, USD 1,400. Bank 500,000; loan 2,000,000; insurance 300,000.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url="
		+ "jdbc:h2:mem:portfolio_dashboard_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@WithMockUser(roles = "OWNER")
class DashboardApiTest {

	private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Seoul"));
	private static final LocalDate START = TODAY.minusDays(120); // under a year: XIRR is a plain period return

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PortfolioSnapshotRepository snapshotRepository;

	@Autowired
	private PortfolioCollectScheduler collectScheduler;

	@Test
	void valuesTheWholePortfolioAndKeepsSnapshotsInSync() throws Exception {
		long brokerage = create("/api/portfolio/accounts", account("BROKERAGE"));
		long bank = create("/api/portfolio/accounts", account("BANK"));
		long loan = create("/api/portfolio/accounts", account("LOAN"));
		long insurance = create("/api/portfolio/accounts", account("INSURANCE"));
		long etf = create("/api/portfolio/instruments", """
				{"symbol":"069500","name":"KODEX 200","market":"KRX","instrumentType":"ETF",
				 "underlyingAssetClass":"EQUITY","exposureRegion":"KR","fxHedged":false,"taxType":"DOMESTIC_EQUITY_ETF"}""");
		long apple = create("/api/portfolio/instruments", """
				{"symbol":"AAPL","name":"Apple","market":"NASDAQ","instrumentType":"STOCK",
				 "underlyingAssetClass":"EQUITY","exposureRegion":"US","fxHedged":false,"taxType":"FOREIGN_STOCK"}""");

		create("/api/portfolio/transactions", cash(brokerage, "OPENING", "1000000", START));
		create("/api/portfolio/transactions", trade(brokerage, etf, "10", "10000", "1"));
		create("/api/portfolio/transactions", trade(brokerage, apple, "2", "100", "1300"));
		long bankDeposit = create("/api/portfolio/transactions", cash(bank, "DEPOSIT", "500000", START));
		create("/api/portfolio/transactions", cash(loan, "LOAN_DRAW", "2000000", START));
		send(post("/api/portfolio/accounts/%d/valuations".formatted(insurance)), """
				{"valuationDate":"%s","amount":300000}""".formatted(TODAY.minusDays(5)))
				.andExpect(status().isOk());

		LocalDate priceDate = TODAY.minusDays(1);
		send(put("/api/portfolio/prices"), """
				{"date":"%s","items":[{"instrumentId":%d,"closePrice":12000},{"instrumentId":%d,"closePrice":110}]}"""
				.formatted(priceDate, etf, apple))
				.andExpect(status().isNoContent());
		send(put("/api/portfolio/fx-rates"), """
				{"date":"%s","items":[{"currency":"USD","rateToKrw":1400}]}""".formatted(priceDate))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/portfolio/dashboard"))
				.andExpect(status().isOk())
				// cash 640,000 + 500,000 | holdings 120,000 + 308,000 | insurance 300,000
				.andExpect(jsonPath("$.summary.totalAssets").value(1868000))
				.andExpect(jsonPath("$.summary.totalLiabilities").value(2000000))
				.andExpect(jsonPath("$.summary.netWorth").value(-132000))
				.andExpect(jsonPath("$.allocation[0].group").value("CASH_EQUIVALENT"))
				.andExpect(jsonPath("$.allocation[0].amount").value(1140000))
				.andExpect(jsonPath("$.allocation[0].weightPercent").value(61.03))
				.andExpect(jsonPath("$.allocation[1].amount").value(428000))
				.andExpect(jsonPath("$.allocation[2].amount").value(300000))
				// scope (brokerage) 1,068,000 from a single 1,000,000 contribution; the bank deposit is outside it
				.andExpect(jsonPath("$.performance.xirr").value(6.8))
				.andExpect(jsonPath("$.performance.xirrAnnualized").value(false))
				.andExpect(jsonPath("$.performance.netContribution").value(1000000))
				.andExpect(jsonPath("$.performance.unrealizedProfit").value(68000))
				.andExpect(jsonPath("$.holdings[0].symbol").value("AAPL"))
				.andExpect(jsonPath("$.holdings[0].marketValueKrw").value(308000))
				.andExpect(jsonPath("$.holdings[0].priceEffect").value(28000))
				.andExpect(jsonPath("$.holdings[0].fxEffect").value(20000))
				.andExpect(jsonPath("$.holdings[0].priceStale").value(false))
				.andExpect(jsonPath("$.holdings[1].fxEffect").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.exposure.byRegion[0].key").value("US"))
				.andExpect(jsonPath("$.exposure.byRegion[0].weightPercent").value(71.96))
				.andExpect(jsonPath("$.exposure.byCurrency[1].key").value("KRW"))
				.andExpect(jsonPath("$.freshness.fxRates[0].currency").value("USD"))
				.andExpect(jsonPath("$.history[-1:].netWorth").value(-132000));

		mockMvc.perform(post("/api/portfolio/snapshots").with(csrf()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.netWorth").value(-132000));

		// Frozen history: entries the snapshot captured can no longer be changed.
		send(put("/api/portfolio/transactions/" + bankDeposit), cash(bank, "DEPOSIT", "600000", START))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("스냅샷")));

		// A new backdated entry is accepted and stays editable, but never rewrites the frozen snapshot.
		long late = create("/api/portfolio/transactions", cash(bank, "DEPOSIT", "100000", START.plusDays(10)));
		assertThat(snapshotRepository.findBySnapshotDate(TODAY).orElseThrow().getNetWorth())
				.isEqualByComparingTo("-132000");
		mockMvc.perform(get("/api/portfolio/transactions").param("accountId", String.valueOf(bank)))
				.andExpect(jsonPath("$.items[0].id").value(late))
				.andExpect(jsonPath("$.items[0].editable").value(true))
				.andExpect(jsonPath("$.items[1].editable").value(false));
		mockMvc.perform(get("/api/portfolio/dashboard"))
				.andExpect(jsonPath("$.summary.netWorth").value(-32000));
		mockMvc.perform(delete("/api/portfolio/transactions/" + late).with(csrf()))
				.andExpect(status().isNoContent());
	}

	@Test
	void manualMarketDataIsValidated() throws Exception {
		send(put("/api/portfolio/fx-rates"), """
				{"date":"%s","items":[{"currency":"KRW","rateToKrw":1}]}""".formatted(TODAY))
				.andExpect(status().isBadRequest());
		send(put("/api/portfolio/prices"), """
				{"date":"%s","items":[{"instrumentId":999999,"closePrice":100}]}""".formatted(TODAY))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/portfolio/dashboard").param("range", "2Y"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void collectorsSkipUntilKeysAreConfigured() throws Exception {
		collectScheduler.collectDomesticPrices();
		collectScheduler.collectFxRates();

		mockMvc.perform(get("/api/portfolio/integrations"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.kis.configured").value(false))
				.andExpect(jsonPath("$.kis.lastResult").value("키 미설정: 수집 건너뜀"))
				.andExpect(jsonPath("$.kexim.configured").value(false));
	}

	private long create(String path, String body) throws Exception {
		String response = send(post(path), body).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	private ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
		return mockMvc.perform(request.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private static String account(String type) {
		return """
				{"name":"%s","institution":"테스트","accountType":"%s"}""".formatted(type, type);
	}

	private static String cash(long account, String type, String amount, LocalDate date) {
		return """
				{"accountId":%d,"type":"%s","tradeDate":"%s","amount":%s}""".formatted(account, type, date, amount);
	}

	private static String trade(long account, long instrument, String quantity, String price, String fx) {
		return """
				{"accountId":%d,"type":"BUY","tradeDate":"%s","instrumentId":%d,"quantity":%s,"price":%s,"fxRate":%s}"""
				.formatted(account, START.plusDays(3), instrument, quantity, price, fx);
	}
}
