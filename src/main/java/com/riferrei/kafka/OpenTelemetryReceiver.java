package com.riferrei.kafka;

import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenTelemetryReceiver implements ClientTelemetryReceiver {

    private static final Logger log = LoggerFactory.getLogger(OpenTelemetryReceiver.class);

    @Override
    public void exportMetrics(AuthorizableRequestContext context, ClientTelemetryPayload payload) {
        String otlpExporter = System.getenv("OTLP_EXPORTER");
        log.info("OTLP Exporter: " + otlpExporter);
    }
    
}
