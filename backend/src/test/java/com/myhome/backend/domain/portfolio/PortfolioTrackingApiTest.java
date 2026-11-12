package com.myhome.backend.domain.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import com.myhome.backend.domain.portfolio.entity.PortfolioSnapshot;
import com.myhome.backend.domain.portfolio.repository.PortfolioSnapshotRepository;
import com.myhome.backend.domain.portfolio.repository.PriceRepository;
import com.myhome.backend.domain.portfolio.service.MarketDataService;
import java.math.BigDecimal;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:portfolio_tracking_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@WithMockUser(roles = "OWNER")
@Transactional
class PortfolioTrackingApiTest {
	private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Seoul"));
	@Autowired private MockMvc mvc;
	@Autowired private PortfolioSnapshotRepository snapshots;
	@Autowired private PriceRepository prices;
	@Autowired private MarketDataService marketData;

	@Test
	void changesSeparateLateContributionsFromGrowthAndCrudRecalculatesImmediately() throws Exception {
		long account = account("BROKERAGE");
		long isa = account("ISA");
		long bank = account("BANK");
		long stock = instrument("KRX", "100001");
		LocalDate monthEnd = TODAY.withDayOfMonth(1).minusDays(1);
		create("transactions", cash(account, "OPENING", 1000, monthEnd.minusDays(3)));
		baseline(monthEnd);
		if (!monthEnd.equals(TODAY.minusDays(1))) baseline(TODAY.minusDays(1));
		long deposit = create("transactions", cash(account, "DEPOSIT", 200, monthEnd.minusDays(2)));
		create("transactions", """
				{"accountId":%d,"type":"BUY","tradeDate":"%s","instrumentId":%d,"quantity":1,"price":100}
				""".formatted(account, TODAY, stock));
		price(stock, "120");
		send(post("/api/portfolio/transfers"), transfer(account, isa, 100)).andExpect(status().isCreated()); // Internal to investment scope.
		create("transactions", cash(bank, "DEPOSIT", 1000, TODAY)); // Outside investment scope.
		send(post("/api/portfolio/transfers"), transfer(bank, account, 30)).andExpect(status().isCreated()); // Crosses into investment scope.
		changes(1220, 230, 20);

		send(put("/api/portfolio/transactions/" + deposit), cash(account, "DEPOSIT", 300, monthEnd.minusDays(2)))
				.andExpect(status().isOk());
		changes(1320, 330, 20);
		mvc.perform(delete("/api/portfolio/transactions/" + deposit).with(csrf())).andExpect(status().isNoContent());
		changes(1020, 30, 20);
		assertThat(snapshots.findBySnapshotDate(monthEnd).orElseThrow().getNetWorth()).isEqualByComparingTo("1000");
	}

	@Test
	void quantityOnlyOpeningUsesStoredReferenceAndManualPricesWinOverCollectors() throws Exception {
		long account = account("BROKERAGE");
		long stock = instrument("NASDAQ", "TEST");
		price(stock, "111");
		send(put("/api/portfolio/fx-rates"), """
				{"date":"%s","items":[{"currency":"USD","rateToKrw":1300}]}
				""".formatted(TODAY)).andExpect(status().isNoContent());
		send(post("/api/portfolio/transactions"), opening(account, stock, "OPENING"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(288600))
				.andExpect(jsonPath("$.price").value(111)).andExpect(jsonPath("$.memo", containsString("시작 기준가")));
		assertThat(marketData.recordCollectedPrice(stock, TODAY, new BigDecimal("999"), false)).isFalse();
		assertThat(prices.findByInstrumentIdAndPriceDate(stock, TODAY).orElseThrow().getClosePrice()).isEqualByComparingTo("111");
		mvc.perform(get("/api/portfolio/dashboard")).andExpect(status().isOk())
				.andExpect(jsonPath("$.summary.netWorth").value(288600))
				.andExpect(jsonPath("$.summary.dayChange").isEmpty())
				.andExpect(jsonPath("$.performance.unrealizedProfit").value(0))
				.andExpect(jsonPath("$.holdings[0].priceSource").value("MANUAL"))
				.andExpect(jsonPath("$.holdings[0].priceCollectedAt", containsString("+09:00")));
		price(stock, "112");
		mvc.perform(get("/api/portfolio/dashboard")).andExpect(jsonPath("$.summary.netWorth").value(291200))
				.andExpect(jsonPath("$.performance.unrealizedProfit").value(2600));
	}

	@Test
	void missingQuoteDoesNotCreateAssetsAndAutomaticBasisIsNotAllowedForTrades() throws Exception {
		long account = account("BROKERAGE");
		long stock = instrument("KRX", "100002");
		send(post("/api/portfolio/transactions"), opening(account, stock, "OPENING"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("시세")));
		send(post("/api/portfolio/transactions"), opening(account, stock, "BUY"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("기초잔고")));
		mvc.perform(get("/api/portfolio/transactions")).andExpect(jsonPath("$.totalElements").value(0));
		mvc.perform(get("/api/portfolio/dashboard")).andExpect(jsonPath("$.summary.totalAssets").value(0));
	}

	private void changes(int total, int contribution, int gain) throws Exception {
		mvc.perform(get("/api/portfolio/dashboard")).andExpect(status().isOk())
				.andExpect(jsonPath("$.summary.dayChange.netWorthChange").value(total))
				.andExpect(jsonPath("$.summary.dayChange.netContribution").value(contribution))
				.andExpect(jsonPath("$.summary.dayChange.investmentGain").value(gain))
				.andExpect(jsonPath("$.summary.monthChange.netContribution").value(contribution))
				.andExpect(jsonPath("$.summary.monthChange.investmentGain").value(gain));
	}

	private void baseline(LocalDate date) {
		PortfolioSnapshot snapshot = new PortfolioSnapshot(date);
		snapshot.record(new BigDecimal("1000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("1000"), date, date.atTime(18, 0));
		snapshots.saveAndFlush(snapshot);
	}

	private long account(String type) throws Exception {
		return create("accounts", """
				{"name":"가상 %s","institution":"테스트","accountType":"%s"}""".formatted(type, type));
	}

	private long instrument(String market, String symbol) throws Exception {
		return create("instruments", """
				{"symbol":"%s","name":"가상 종목","market":"%s","instrumentType":"STOCK",
				 "underlyingAssetClass":"EQUITY","exposureRegion":"US","fxHedged":false,"taxType":"FOREIGN_STOCK"}
				""".formatted(symbol, market));
	}

	private String cash(long account, String type, int amount, LocalDate date) {
		return """
				{"accountId":%d,"type":"%s","tradeDate":"%s","amount":%d}""".formatted(account, type, date, amount);
	}

	private String opening(long account, long stock, String type) {
		return """
				{"accountId":%d,"instrumentId":%d,"type":"%s","tradeDate":"%s","quantity":2,"useLatestPrice":true}
				""".formatted(account, stock, type, TODAY);
	}

	private String transfer(long from, long to, int amount) {
		return """
				{"fromAccountId":%d,"toAccountId":%d,"tradeDate":"%s","amount":%d}""".formatted(from, to, TODAY, amount);
	}

	private void price(long stock, String price) throws Exception {
		send(put("/api/portfolio/prices"), """
				{"date":"%s","items":[{"instrumentId":%d,"closePrice":%s}]}""".formatted(TODAY, stock, price))
				.andExpect(status().isNoContent());
	}

	private long create(String path, String body) throws Exception {
		return ((Number) JsonPath.read(send(post("/api/portfolio/" + path), body).andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
	}

	private ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
		return mvc.perform(request.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
	}
}
