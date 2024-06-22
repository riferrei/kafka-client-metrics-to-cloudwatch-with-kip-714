package com.riferrei.kafka;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.time.Duration;

import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenTelemetryReceiver implements ClientTelemetryReceiver {

    private final Logger log = LoggerFactory.getLogger(OpenTelemetryReceiver.class);

    private String otlpMetricsEndpoint = "http://localhost:4318/v1/metrics";
    private int otlpMetricsTimeout = 10000;
    private HttpClient client;


    public OpenTelemetryReceiver() {
        var otlpMetricsEndpointEnv = System.getenv("OTEL_EXPORTER_OTLP_METRICS_ENDPOINT");
        if (otlpMetricsEndpointEnv != null && !otlpMetricsEndpointEnv.isEmpty()) {
            otlpMetricsEndpoint = otlpMetricsEndpointEnv;
        }
        var otlpMetricsTimeoutEnv = System.getenv("OTEL_EXPORTER_OTLP_METRICS_TIMEOUT");
        if (otlpMetricsTimeoutEnv != null && !otlpMetricsTimeoutEnv.isEmpty()) {
            otlpMetricsTimeout = Integer.parseInt(otlpMetricsTimeoutEnv);
        }
        client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofMillis(otlpMetricsTimeout))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    @Override
    public void exportMetrics(AuthorizableRequestContext context, ClientTelemetryPayload payload) {
        ByteBuffer byteBuffer = payload.data();
        byte[] bytes;

        if (byteBuffer.hasArray()) {
            bytes = byteBuffer.array();
        } else {
            bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(otlpMetricsEndpoint))
            .header("Content-Type", "application/x-protobuf")
            .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
            .build();

        try {
            client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    log.info("OTLP metrics endpoint response status code: " + response.statusCode());
                    return response;
                })
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    log.error("Error invoking the OTLP metrics endpoint", ex);
                    return null;
                })
                .join();
        } catch (Exception ex) {
            log.error("Unhandled exception", ex);
        }
    }
    
}
