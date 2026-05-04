package com.myhome.backend.domain.monitoring.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NodeExporterMetricParserTest {

	private final NodeExporterMetricParser parser = new NodeExporterMetricParser();

	@Test
	void parseExtractsSummaryMetrics() {
		String metrics = """
				node_cpu_seconds_total{cpu="0",mode="idle"} 100
				node_cpu_seconds_total{cpu="0",mode="user"} 40
				node_cpu_seconds_total{cpu="0",mode="system"} 10
				node_memory_MemTotal_bytes 1000
				node_memory_MemAvailable_bytes 250
				node_filesystem_size_bytes{device="/dev/root",fstype="ext4",mountpoint="/"} 2000
				node_filesystem_avail_bytes{device="/dev/root",fstype="ext4",mountpoint="/"} 500
				node_load1 0.42
				node_hwmon_temp_celsius{chip="thermal_thermal_zone0",sensor="temp1"} 51.5
				""";

		NodeExporterMetrics result = parser.parse(metrics);

		assertThat(result.cpuTotalSeconds()).isEqualTo(150.0);
		assertThat(result.cpuIdleSeconds()).isEqualTo(100.0);
		assertThat(result.memoryUsagePercent()).isEqualTo(75.0);
		assertThat(result.memoryAvailableBytes()).isEqualTo(250.0);
		assertThat(result.memoryTotalBytes()).isEqualTo(1000.0);
		assertThat(result.diskUsagePercent()).isEqualTo(75.0);
		assertThat(result.diskAvailableBytes()).isEqualTo(500.0);
		assertThat(result.diskTotalBytes()).isEqualTo(2000.0);
		assertThat(result.loadAverage1m()).isEqualTo(0.42);
		assertThat(result.temperatureCelsius()).isEqualTo(51.5);
	}
}
