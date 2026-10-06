/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * Podman request processor
 */
@AllArgsConstructor
public final class HttpClientPodmanRequestProcessor implements PodmanRequestProcessor {

    private final @NonNull Supplier<HttpClient> clientFactory;

    public HttpClientPodmanRequestProcessor() {
        this(HttpClient::newHttpClient);
    }

    @Override
    @NonNull public PodmanResponse<JsonNode> apply(@NonNull ObjectMapper mapper, @NonNull HttpRequest request)
            throws IOException, InterruptedException {
        try (var client = clientFactory.get()) {
            return response(client.send(request, new JacksonNodeBodyHandler(mapper)));
        }
    }

    @Override
    @NonNull public PodmanResponse<InputStream> stream(@NonNull HttpRequest request) throws IOException, InterruptedException {
        var client = clientFactory.get();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            return new PodmanResponse<>(response.statusCode(), response.uri(), closingStream(response.body(), client));
        } catch (IOException | InterruptedException | RuntimeException e) {
            client.close();
            throw e;
        }
    }

    @NonNull private static <T> PodmanResponse<T> response(@NonNull HttpResponse<T> response) {
        return new PodmanResponse<>(response.statusCode(), response.uri(), response.body());
    }

    @NonNull private static InputStream closingStream(@NonNull InputStream body, @NonNull HttpClient client) {
        return new FilterInputStream(body) {

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    client.close();
                }
            }
        };
    }
}
