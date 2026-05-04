package com.myhome.backend.domain.monitoring.client;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NodeExporterMetricParser {

	private static final String ROOT_MOUNTPOINT = "/";

	private final Clock clock = Clock.systemUTC();

	public NodeExporterMetrics parse(String metricsText) {
		Map<String, Double> simpleMetrics = new HashMap<>();
		double cpuTotalSeconds = 0.0;
		double cpuIdleSeconds = 0.0;
		Double rootFilesystemSizeBytes = null;
		Double rootFilesystemAvailableBytes = null;
		Double temperatureCelsius = null;

		for (String line : metricsText.split("\\R")) {
			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			MetricSample sample = MetricSample.parse(line);
			if (sample == null) {
				continue;
			}

			if (!sample.hasLabels()) {
				simpleMetrics.put(sample.name(), sample.value());
			}

			if ("node_cpu_seconds_total".equals(sample.name())) {
				cpuTotalSeconds += sample.value();
				if ("idle".equals(sample.labels().get("mode"))) {
					cpuIdleSeconds += sample.value();
				}
			}

			if ("node_filesystem_size_bytes".equals(sample.name()) && isRootFilesystem(sample)) {
				rootFilesystemSizeBytes = sample.value();
			}

			if ("node_filesystem_avail_bytes".equals(sample.name()) && isRootFilesystem(sample)) {
				rootFilesystemAvailableBytes = sample.value();
			}

			if (temperatureCelsius == null && isTemperatureMetric(sample.name())) {
				temperatureCelsius = normalizeTemperature(sample.value());
			}
		}

		Double memoryUsagePercent = calculateMemoryUsagePercent(simpleMetrics);
		Double diskUsagePercent = calculateDiskUsagePercent(rootFilesystemSizeBytes, rootFilesystemAvailableBytes);
		Long uptimeSeconds = calculateUptimeSeconds(simpleMetrics.get("node_boot_time_seconds"));

		return new NodeExporterMetrics(
				cpuTotalSeconds == 0.0 ? null : cpuTotalSeconds,
				cpuIdleSeconds == 0.0 ? null : cpuIdleSeconds,
				memoryUsagePercent,
				simpleMetrics.get("node_memory_MemAvailable_bytes"),
				simpleMetrics.get("node_memory_MemTotal_bytes"),
				diskUsagePercent,
				rootFilesystemAvailableBytes,
				rootFilesystemSizeBytes,
				uptimeSeconds,
				simpleMetrics.get("node_load1"),
				temperatureCelsius
		);
	}

	private boolean isRootFilesystem(MetricSample sample) {
		String mountpoint = sample.labels().get("mountpoint");
		String filesystemType = sample.labels().get("fstype");
		return ROOT_MOUNTPOINT.equals(mountpoint)
				&& filesystemType != null
				&& !filesystemType.startsWith("tmpfs")
				&& !"overlay".equals(filesystemType);
	}

	private boolean isTemperatureMetric(String name) {
		return "node_thermal_zone_temp".equals(name) || "node_hwmon_temp_celsius".equals(name);
	}

	private Double normalizeTemperature(Double value) {
		if (value == null) {
			return null;
		}
		return value > 1_000 ? value / 1_000.0 : value;
	}

	private Double calculateMemoryUsagePercent(Map<String, Double> metrics) {
		Double totalBytes = metrics.get("node_memory_MemTotal_bytes");
		Double availableBytes = metrics.get("node_memory_MemAvailable_bytes");
		if (totalBytes == null || availableBytes == null || totalBytes <= 0) {
			return null;
		}
		return percent((totalBytes - availableBytes) / totalBytes);
	}

	private Double calculateDiskUsagePercent(Double sizeBytes, Double availableBytes) {
		if (sizeBytes == null || availableBytes == null || sizeBytes <= 0) {
			return null;
		}
		return percent((sizeBytes - availableBytes) / sizeBytes);
	}

	private Long calculateUptimeSeconds(Double bootTimeSeconds) {
		if (bootTimeSeconds == null) {
			return null;
		}
		long nowEpochSeconds = clock.instant().getEpochSecond();
		return Math.max(0, nowEpochSeconds - bootTimeSeconds.longValue());
	}

	private Double percent(double ratio) {
		return Math.round(ratio * 10_000.0) / 100.0;
	}

	private record MetricSample(String name, Map<String, String> labels, Double value) {

		static MetricSample parse(String line) {
			String[] parts = line.trim().split("\\s+", 2);
			if (parts.length != 2) {
				return null;
			}

			Double value = parseDouble(parts[1]);
			if (value == null) {
				return null;
			}

			String nameAndLabels = parts[0];
			int labelStart = nameAndLabels.indexOf('{');
			if (labelStart < 0) {
				return new MetricSample(nameAndLabels, Map.of(), value);
			}

			String name = nameAndLabels.substring(0, labelStart);
			String labelText = nameAndLabels.substring(labelStart + 1, nameAndLabels.length() - 1);
			return new MetricSample(name, parseLabels(labelText), value);
		}

		boolean hasLabels() {
			return !labels.isEmpty();
		}

		private static Double parseDouble(String value) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException exception) {
				return null;
			}
		}

		private static Map<String, String> parseLabels(String labelText) {
			Map<String, String> labels = new HashMap<>();
			for (String label : labelText.split(",")) {
				String[] keyValue = label.split("=", 2);
				if (keyValue.length == 2) {
					labels.put(keyValue[0], keyValue[1].replace("\"", ""));
				}
			}
			return labels;
		}
	}
}
