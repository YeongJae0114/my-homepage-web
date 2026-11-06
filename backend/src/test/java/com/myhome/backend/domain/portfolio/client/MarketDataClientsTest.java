package com.myhome.backend.domain.portfolio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.myhome.backend.domain.portfolio.config.PortfolioProperties;
import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Fixtures follow the providers' documented response shapes; re-check against real responses once keys exist. */
class MarketDataClientsTest {

	@Test
	void kisIssuesOneTokenAndParsesDomesticAndOverseasQuotes() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://kis.test");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		server.expect(once(), requestTo("https://kis.test/oauth2/tokenP"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.grant_type").value("client_credentials"))
				.andExpect(jsonPath("$.appkey").value("app-key"))
				.andRespond(withSuccess("""
						{"access_token":"token-1","token_type":"Bearer","expires_in":86400}""", MediaType.APPLICATION_JSON));
		server.expect(requestTo(startsWith("https://kis.test/uapi/domestic-stock/v1/quotations/inquire-price")))
				.andExpect(queryParam("FID_INPUT_ISCD", "069500"))
				.andExpect(header("authorization", "Bearer token-1"))
				.andExpect(header("appkey", "app-key"))
				.andExpect(header("tr_id", "FHKST01010100"))
				.andRespond(withSuccess("""
						{"rt_cd":"0","msg_cd":"MCA00000","msg1":"정상처리 되었습니다.",
						 "output":{"stck_prpr":"35250","prdy_vrss":"150"}}""", MediaType.APPLICATION_JSON));
		server.expect(requestTo(startsWith("https://kis.test/uapi/overseas-price/v1/quotations/price")))
				.andExpect(queryParam("EXCD", "NAS"))
				.andExpect(queryParam("SYMB", "AAPL"))
				.andExpect(header("tr_id", "HHDFS00000300"))
				.andRespond(withSuccess("""
						{"rt_cd":"0","msg1":"정상처리 되었습니다.","output":{"rsym":"DNASAAPL","last":"190.1200"}}""",
						MediaType.APPLICATION_JSON));

		KisClient client = new KisClient(new PortfolioProperties.Kis("https://kis.test", "app-key", "app-secret"), builder.build());

		assertThat(client.fetchDomesticPrice("069500")).isEqualByComparingTo("35250");
		assertThat(client.fetchOverseasPrice(Market.NASDAQ, "AAPL")).isEqualByComparingTo("190.12");
		server.verify(); // the second quote reused the cached token
	}

	@Test
	void kisErrorOrMissingPriceIsRejected() {
		assertThatThrownBy(() -> KisClient.priceFrom(new KisClient.QuoteResponse("1", "조회 실패", Map.of()), "stck_prpr", "069500"))
				.hasMessageContaining("조회 실패");
		assertThatThrownBy(() -> KisClient.priceFrom(
				new KisClient.QuoteResponse("0", "정상", Map.of("last", "")), "last", "AAPL"))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void keximParsesBaseRatesAndNormalizesPerHundredQuotes() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://kexim.test");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		server.expect(requestTo(startsWith("https://kexim.test/site/program/financial/exchangeJSON")))
				.andExpect(queryParam("authkey", "secret-key"))
				.andExpect(queryParam("searchdate", "20261106"))
				.andExpect(queryParam("data", "AP01"))
				.andRespond(withSuccess("""
						[{"result":1,"cur_unit":"USD","cur_nm":"미국 달러","deal_bas_r":"1,383.5"},
						 {"result":1,"cur_unit":"JPY(100)","cur_nm":"일본 옌","deal_bas_r":"912.34"},
						 {"result":1,"cur_unit":"KRW","cur_nm":"한국 원","deal_bas_r":"1"}]""", MediaType.APPLICATION_JSON));

		Map<String, BigDecimal> rates = keximClient(builder).fetchRates(LocalDate.of(2026, 11, 6));

		assertThat(rates).containsOnlyKeys("USD", "JPY");
		assertThat(rates.get("USD")).isEqualByComparingTo("1383.5");
		assertThat(rates.get("JPY")).isEqualByComparingTo("9.1234");
	}

	@Test
	void keximHolidayIsEmptyAndFailuresNeverLeakTheKey() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://kexim.test");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		server.expect(requestTo(startsWith("https://kexim.test/"))).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		server.expect(requestTo(startsWith("https://kexim.test/"))).andRespond(withServerError());
		KeximFxClient client = keximClient(builder);

		assertThat(client.fetchRates(LocalDate.of(2026, 11, 7))).isEmpty();
		assertThatThrownBy(() -> client.fetchRates(LocalDate.of(2026, 11, 9)))
				.isInstanceOf(IllegalStateException.class)
				.satisfies(exception -> assertThat(exception.getMessage()).doesNotContain("secret-key"));
		assertThat(KeximFxClient.redact("GET https://x/?authkey=secret-key&data=AP01")).doesNotContain("secret-key");
	}

	private static KeximFxClient keximClient(RestClient.Builder builder) {
		return new KeximFxClient(new PortfolioProperties.Kexim("https://kexim.test", "secret-key"), builder.build());
	}
}
