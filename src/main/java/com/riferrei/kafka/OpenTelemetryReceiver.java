package com.riferrei.kafka;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenTelemetryReceiver implements ClientTelemetryReceiver {

    private final Logger log = LoggerFactory.getLogger(OpenTelemetryReceiver.class);

    private final String otlpMetricsEndpoint;
    private final HttpClient httpClient;

    public OpenTelemetryReceiver(String otlpMetricsEndpoint, HttpClient httpClient) {
        this.otlpMetricsEndpoint = Objects.requireNonNull(otlpMetricsEndpoint);
        this.httpClient = Objects.requireNonNull(httpClient);
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

        httpClient.sendAsync(request, BodyHandlers.ofString())
            .thenApply(response -> {
                log.info("OTLP metrics endpoint response status code: " + response.statusCode());
                log.debug("OTLP metrics endpoint response: {}", response.body());
                return response;
            })
            .thenApply(HttpResponse::body)
            .exceptionally(ex -> {
                log.error("Error invoking the OTLP metrics endpoint", ex);
                return null;
            });
    }
    
}
