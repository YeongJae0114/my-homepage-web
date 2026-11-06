package com.myhome.backend.domain.portfolio.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.myhome.backend.domain.portfolio.config.PortfolioProperties;
import com.myhome.backend.domain.portfolio.entity.Instrument.Market;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Korea Investment & Securities Open API, QUOTES ONLY.
 * The real-account app key can also place orders: this class deliberately has no order or balance calls.
 * Keys and tokens travel in headers only and are never logged.
 * Endpoint paths, TR IDs and field names follow the KIS developer docs; re-check them when the key is issued.
 */
@Component
public class KisClient {

	private static final Duration TOKEN_REFRESH_MARGIN = Duration.ofMinutes(10);
	private static final Map<Market, String> OVERSEAS_EXCHANGE = Map.of(
			Market.NASDAQ, "NAS",
			Market.NYSE, "NYS",
			Market.AMEX, "AMS");

	private final PortfolioProperties.Kis properties;
	private final RestClient restClient;
	private String accessToken;
	private Instant tokenExpiresAt = Instant.MIN;

	@Autowired
	public KisClient(PortfolioProperties properties) {
		this(properties.kis(), RestClient.builder()
				.baseUrl(properties.kis().baseUrl())
				.requestFactory(timeouts(properties.requestTimeout()))
				.build());
	}

	KisClient(PortfolioProperties.Kis properties, RestClient restClient) {
		this.properties = properties;
		this.restClient = restClient;
	}

	public boolean isConfigured() {
		return properties.configured();
	}

	/** Current (or, after the close, closing) price of a KRX stock or ETF. */
	public BigDecimal fetchDomesticPrice(String symbol) {
		QuoteResponse response = restClient.get()
				.uri(uri -> uri.path("/uapi/domestic-stock/v1/quotations/inquire-price")
						.queryParam("FID_COND_MRKT_DIV_CODE", "J")
						.queryParam("FID_INPUT_ISCD", symbol)
						.build())
				.headers(headers -> authorize(headers, "FHKST01010100"))
				.retrieve()
				.body(QuoteResponse.class);
		return priceFrom(response, "stck_prpr", symbol);
	}

	/** Last price of a US-listed stock or ETF; after the US close this is the closing price. */
	public BigDecimal fetchOverseasPrice(Market market, String symbol) {
		String exchange = OVERSEAS_EXCHANGE.get(market);
		if (exchange == null) {
			throw new IllegalArgumentException("Not an overseas market: " + market);
		}
		QuoteResponse response = restClient.get()
				.uri(uri -> uri.path("/uapi/overseas-price/v1/quotations/price")
						.queryParam("AUTH", "")
						.queryParam("EXCD", exchange)
						.queryParam("SYMB", symbol)
						.build())
				.headers(headers -> authorize(headers, "HHDFS00000300"))
				.retrieve()
				.body(QuoteResponse.class);
		return priceFrom(response, "last", symbol);
	}

	private void authorize(HttpHeaders headers, String trId) {
		headers.setBearerAuth(token());
		headers.set("appkey", properties.appKey());
		headers.set("appsecret", properties.appSecret());
		headers.set("tr_id", trId);
		headers.set("custtype", "P");
	}

	/** Cached; reissued only shortly before expiry (KIS throttles frequent token issuance). */
	private synchronized String token() {
		if (accessToken != null && Instant.now().isBefore(tokenExpiresAt.minus(TOKEN_REFRESH_MARGIN))) {
			return accessToken;
		}
		TokenResponse response = restClient.post()
				.uri("/oauth2/tokenP")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of(
						"grant_type", "client_credentials",
						"appkey", properties.appKey(),
						"appsecret", properties.appSecret()))
				.retrieve()
				.body(TokenResponse.class);
		if (response == null || !StringUtils.hasText(response.accessToken())) {
			throw new IllegalStateException("KIS token response was empty");
		}
		accessToken = response.accessToken();
		tokenExpiresAt = Instant.now().plusSeconds(response.expiresIn() == null ? 3600 : response.expiresIn());
		return accessToken;
	}

	static BigDecimal priceFrom(QuoteResponse response, String field, String symbol) {
		if (response == null) {
			throw new IllegalStateException("KIS %s: empty response".formatted(symbol));
		}
		if (!"0".equals(response.rtCd())) {
			throw new IllegalStateException("KIS %s: %s".formatted(symbol, response.msg1()));
		}
		Object value = response.output() == null ? null : response.output().get(field);
		String text = value == null ? "" : value.toString().replace(",", "").trim();
		if (text.isEmpty()) {
			throw new IllegalStateException("KIS %s: no %s in response".formatted(symbol, field));
		}
		BigDecimal price = new BigDecimal(text);
		if (price.signum() <= 0) {
			throw new IllegalStateException("KIS %s: no trade price (%s)".formatted(symbol, text));
		}
		return price;
	}

	private static SimpleClientHttpRequestFactory timeouts(Duration timeout) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(timeout);
		factory.setReadTimeout(timeout);
		return factory;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record TokenResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") Long expiresIn) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record QuoteResponse(@JsonProperty("rt_cd") String rtCd, @JsonProperty("msg1") String msg1, Map<String, Object> output) {
	}
}
