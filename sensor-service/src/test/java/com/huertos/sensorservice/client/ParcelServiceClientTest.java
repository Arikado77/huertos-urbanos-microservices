package com.huertos.sensorservice.client;

import com.huertos.sensorservice.exception.ResourceNotFoundException;
import com.huertos.sensorservice.exception.UpstreamServiceException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParcelServiceClientTest {
    @Test
    void acceptsSuccessfulParcelLookup() throws IOException {
        HttpServer server = serverWithStatus(200);
        try {
            assertDoesNotThrow(() -> client(server).assertParcelExists(1L));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mapsUnknownParcelToNotFound() throws IOException {
        HttpServer server = serverWithStatus(404);
        try {
            assertThrows(ResourceNotFoundException.class, () -> client(server).assertParcelExists(404L));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mapsUpstreamServerErrorToServiceUnavailableException() throws IOException {
        HttpServer server = serverWithStatus(500);
        try {
            assertThrows(UpstreamServiceException.class, () -> client(server).assertParcelExists(1L));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mapsConnectionRefusalToServiceUnavailableException() throws IOException {
        HttpServer server = serverWithStatus(200);
        int port = server.getAddress().getPort();
        server.stop(0);

        ParcelServiceClient unavailableClient = new ParcelServiceClient(
                RestClient.builder(),
                new ParcelServiceClient.ParcelServiceClientProperties(
                        "http://127.0.0.1:" + port,
                        Duration.ofMillis(300),
                        Duration.ofMillis(300)
                )
        );

        assertThrows(UpstreamServiceException.class, () -> unavailableClient.assertParcelExists(1L));
    }

    private static ParcelServiceClient client(HttpServer server) {
        return new ParcelServiceClient(
                RestClient.builder(),
                new ParcelServiceClient.ParcelServiceClientProperties(
                        "http://127.0.0.1:" + server.getAddress().getPort(),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(1)
                )
        );
    }

    private static HttpServer serverWithStatus(int status) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/parcels/", exchange -> {
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        return server;
    }
}
