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

    private static final Logger log = LoggerFactory.getLogger(OpenTelemetryReceiver.class);

    private final HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    @Override
    public void exportMetrics(AuthorizableRequestContext context, ClientTelemetryPayload payload) {
        String otlpExporter = System.getenv("OTLP_EXPORTER");
        ByteBuffer byteBuffer = payload.data();
        byte[] bytes;

        if (byteBuffer.hasArray()) {
            bytes = byteBuffer.array();
        } else {
            bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(otlpExporter + "/v1/metrics"))
            .header("Content-Type", "application/x-protobuf")
            .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
            .build();

        try {
            client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    log.info("Response status code: " + response.statusCode());
                    return response;
                })
                .thenApply(HttpResponse::body);
        } catch (Exception ex) {
            log.error("Error sending request to OTLP exporter", ex);
        }
    }
    
}
