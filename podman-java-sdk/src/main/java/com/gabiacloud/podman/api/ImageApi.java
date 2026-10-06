/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.util.PodmanResponseUtil;
import com.gabiacloud.podman.util.QueryUrlUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Image lookup and pull
 */
@AllArgsConstructor
public final class ImageApi {

    private static final URI BASE = URI.create("/v6.0.0/libpod/images/");

    private final @NonNull PodmanProperty properties;

    private final @NonNull ObjectMapper mapper;

    private final @NonNull PodmanRequestExecutor executor;

    /**
     * Checks whether an image exists
     *
     * @return {@code false} instead of an exception if missing
     * @throws PodmanClientException if the connection fails
     */
    public boolean exists(@NonNull String reference) {
        var request =
                properties.newRequest(BASE.resolve(reference + "/exists")).GET().build();

        return PodmanResponseUtil.exists(executor.process(request));
    }

    /**
     * Pulls an image and waits until the pull completes
     *
     * @return ID of the pulled image
     * @throws PodmanClientException if the connection fails or the body cannot be read
     * @throws PodmanApiException if the pull fails
     */
    @NonNull public String pull(@NonNull String reference) {
        var uri = QueryUrlUtil.addQuery(BASE.resolve("pull"), Map.of("reference", reference));
        var request = properties
                .newRequest(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        var response = executor.stream(request);
        try (var body = response.getBody();
                var reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
            if (response.getStatusCode() >= 400) {
                throw toException(request.uri(), response.getStatusCode(), reader);
            }

            return readPulledId(request.uri(), response.getStatusCode(), reader);
        } catch (IOException e) {
            throw new PodmanClientException("cannot read pull progress of " + reference, e);
        }
    }

    @NonNull private PodmanApiException toException(@NonNull URI uri, int statusCode, @NonNull BufferedReader reader) {
        var body = reader.lines().collect(Collectors.joining(" "));

        JsonNode parsed = null;
        try {
            parsed = mapper.readTree(body);
        } catch (IOException ignored) {
            // ignored
        }

        return PodmanResponseUtil.toException(uri, statusCode, parsed, body);
    }

    @NonNull private String readPulledId(@NonNull URI uri, int statusCode, @NonNull BufferedReader reader) throws IOException {
        String id = null;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            var node = mapper.readTree(line);
            var error = text(node.get("error"));
            if (error != null) {
                throw new PodmanApiException(uri, statusCode, error);
            }

            var pulled = text(node.get("id"));
            if (pulled != null) {
                id = pulled;
            }
        }

        if (id == null) {
            throw new PodmanApiException(uri, statusCode, "pull finished without an image id");
        }

        return id;
    }

    @Nullable private static String text(@Nullable JsonNode node) {
        return node == null || node.isNull() || node.asText().isBlank() ? null : node.asText();
    }
}
