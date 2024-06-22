package com.riferrei.kafka;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.server.telemetry.ClientTelemetry;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KIP714MetricReporter implements MetricsReporter, ClientTelemetry {

    private static final Logger log = LoggerFactory.getLogger(KIP714MetricReporter.class);

    private String otlpMetricsEndpoint = "http://localhost:4318/v1/metrics";
    private int otlpMetricsConnectTimeout = 5000;
    private HttpClient httpClient;

    @Override
    public void init(List<KafkaMetric> metrics) {
        log.debug("Initializing the KIP-714 metric reporter: " + metrics);
    }

    @Override
    public void configure(Map<String, ?> configs) {
        var otlpMetricsEndpointEnv = System.getenv("OTEL_EXPORTER_OTLP_METRICS_ENDPOINT");
        if (otlpMetricsEndpointEnv != null && !otlpMetricsEndpointEnv.isEmpty()) {
            otlpMetricsEndpoint = otlpMetricsEndpointEnv;
        }
        var otlpMetricsConnectTimeoutEnv = System.getenv("OTEL_EXPORTER_OTLP_METRICS_CONNECT_TIMEOUT");
        if (otlpMetricsConnectTimeoutEnv != null && !otlpMetricsConnectTimeoutEnv.isEmpty()) {
            otlpMetricsConnectTimeout = Integer.parseInt(otlpMetricsConnectTimeoutEnv);
        }
        httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofMillis(otlpMetricsConnectTimeout))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    @Override
    public void metricChange(KafkaMetric metric) {
        log.info("Changing the metric: " + metric.metricName());
    }

    @Override
    public void metricRemoval(KafkaMetric metric) {
        log.info("Removing the metric: " + metric.metricName());
    }

    @Override
    public void close() {
        log.debug("Closing the KIP-714 metric reporter");
        httpClient.close();
    }

    @Override
    public ClientTelemetryReceiver clientReceiver() {
        return new OpenTelemetryReceiver(otlpMetricsEndpoint, httpClient);
    }

}
