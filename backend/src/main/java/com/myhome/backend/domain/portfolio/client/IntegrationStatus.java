package com.myhome.backend.domain.portfolio.client;

import com.myhome.backend.domain.portfolio.dto.MarketDataDtos.SourceStatus;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Last collection result per source, for the owner UI. In memory: resets on restart, which is fine for a status line. */
@Component
public class IntegrationStatus {

	public static final String KIS = "KIS";
	public static final String KEXIM = "KEXIM";

	private final Map<String, SourceStatus> latest = new ConcurrentHashMap<>();

	public void record(String source, boolean configured, String result) {
		latest.put(source, new SourceStatus(configured, OffsetDateTime.now(ZoneId.of("Asia/Seoul")), result));
	}

	public SourceStatus get(String source, boolean configured) {
		SourceStatus status = latest.get(source);
		return status == null ? new SourceStatus(configured, null, null) : new SourceStatus(configured, status.lastRunAt(), status.lastResult());
	}
}
