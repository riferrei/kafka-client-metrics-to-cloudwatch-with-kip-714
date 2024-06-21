package com.riferrei.kafka;

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

    @Override
    public void init(List<KafkaMetric> metrics) {
        log.info("Initializing the metrics");
    }

    @Override
    public void configure(Map<String, ?> configs) {
        log.info("Configuring the reporter");
    }

    @Override
    public void metricChange(KafkaMetric metric) {
        log.info("Handling the change in a metric");
    }

    @Override
    public void metricRemoval(KafkaMetric metric) {
        log.info("Handling the removal of a metric");
    }

    @Override
    public void close() {
        log.info("Closing the reporter");
    }

    @Override
    public ClientTelemetryReceiver clientReceiver() {
        return new OpenTelemetryReceiver();
    }

}
