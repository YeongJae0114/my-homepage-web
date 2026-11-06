package com.myhome.backend.domain.portfolio.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.myhome.backend.domain.portfolio.config.PortfolioProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Korea Eximbank daily exchange rates (deal_bas_r, base rate). Published on business days around 11:00 KST;
 * an empty list means a holiday or not published yet. The API key travels in the query string, so error
 * messages are redacted before they can reach a log.
 */
@Component
public class KeximFxClient {

	private final PortfolioProperties.Kexim properties;
	private final RestClient restClient;

	@Autowired
	public KeximFxClient(PortfolioProperties properties) {
		this(properties.kexim(), RestClient.builder()
				.baseUrl(properties.kexim().baseUrl())
				.requestFactory(timeouts(properties.requestTimeout()))
				.build());
	}

	KeximFxClient(PortfolioProperties.Kexim properties, RestClient restClient) {
		this.properties = properties;
		this.restClient = restClient;
	}

	public boolean isConfigured() {
		return properties.configured();
	}

	/** KRW per ONE unit, by ISO currency code. JPY(100) and similar per-100 quotes are normalized. */
	public Map<String, BigDecimal> fetchRates(LocalDate date) {
		List<KeximRate> rows;
		try {
			rows = restClient.get()
					.uri(uri -> uri.path("/site/program/financial/exchangeJSON")
							.queryParam("authkey", properties.apiKey())
							.queryParam("searchdate", date.format(DateTimeFormatter.BASIC_ISO_DATE))
							.queryParam("data", "AP01")
							.build())
					.retrieve()
					.body(new ParameterizedTypeReference<List<KeximRate>>() {
					});
		} catch (RestClientException exception) {
			// Not chained on purpose: the cause's message contains the request URL with the key.
			throw new IllegalStateException("KEXIM request failed: " + redact(exception.getMessage()));
		}
		return parse(rows);
	}

	static Map<String, BigDecimal> parse(List<KeximRate> rows) {
		Map<String, BigDecimal> rates = new LinkedHashMap<>();
		if (rows == null) {
			return rates;
		}
		for (KeximRate row : rows) {
			if (row.result() != null && row.result() != 1) {
				// 2: bad data code, 3: bad auth key, 4: daily call limit reached
				throw new IllegalStateException("KEXIM returned result=" + row.result());
			}
			if (row.currencyUnit() == null || row.baseRate() == null) {
				continue;
			}
			String unit = row.currencyUnit().trim();
			int open = unit.indexOf('(');
			String currency = open < 0 ? unit : unit.substring(0, open).trim();
			BigDecimal perUnits = open < 0 ? BigDecimal.ONE : new BigDecimal(unit.substring(open + 1, unit.indexOf(')')));
			if (currency.length() != 3 || "KRW".equals(currency)) {
				continue;
			}
			BigDecimal quoted = new BigDecimal(row.baseRate().replace(",", "").trim());
			rates.put(currency, quoted.divide(perUnits, 6, RoundingMode.HALF_UP));
		}
		return rates;
	}

	static String redact(String message) {
		return message == null ? null : message.replaceAll("authkey=[^&\\s\"]+", "authkey=***");
	}

	private static SimpleClientHttpRequestFactory timeouts(Duration timeout) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(timeout);
		factory.setReadTimeout(timeout);
		return factory;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record KeximRate(
			Integer result,
			@JsonProperty("cur_unit") String currencyUnit,
			@JsonProperty("deal_bas_r") String baseRate
	) {
	}
}
