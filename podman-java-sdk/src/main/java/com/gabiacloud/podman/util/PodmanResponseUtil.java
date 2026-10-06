/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.component.PodmanResponse;
import com.gabiacloud.podman.exception.PodmanApiException;
import java.net.URI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Handles response status codes and libpod error bodies
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PodmanResponseUtil {

    private static final String UNKNOWN_ERROR = "Unknown error";

    private static final int NOT_FOUND = 404;

    /**
     * Reads a response from an {@code exists} endpoint
     *
     * @return {@code false} on 404, {@code true} on any other success
     * @throws PodmanApiException on any other failure
     */
    public static boolean exists(@NonNull PodmanResponse<JsonNode> response) {
        handleDefault(response);

        return response.getStatusCode() != NOT_FOUND;
    }

    /**
     * Reads a single-item response into a model
     *
     * @return {@code null} on 404
     * @throws PodmanApiException on any other failure
     */
    @Nullable public static <T> T findOne(
            @NonNull ObjectMapper mapper, @NonNull PodmanResponse<JsonNode> response, @NonNull Class<T> type) {
        handleDefault(response);
        if (response.getStatusCode() == NOT_FOUND) {
            return null;
        }

        return mapper.convertValue(response.getBody(), type);
    }

    /**
     * Throws on errors other than 404
     *
     * @throws PodmanApiException if the status code is 400 or higher and not 404
     */
    public static void handleDefault(@NonNull PodmanResponse<JsonNode> response) {
        if (response.getStatusCode() == NOT_FOUND || response.getStatusCode() < 400) {
            return;
        }

        throw toException(response);
    }

    /**
     * Throws on every error, including 404
     *
     * @throws PodmanApiException if the status code is 400 or higher
     */
    public static void error404(@NonNull PodmanResponse<JsonNode> response) {
        if (response.getStatusCode() >= 400) {
            throw toException(response);
        }
    }

    /**
     * Checks per-container errors in a pod operation response
     *
     * @throws PodmanApiException if {@code Errs} is not empty
     */
    public static void assertNoContainerErrors(@NonNull PodmanResponse<JsonNode> response) {
        var errs = response.getBody() == null ? null : response.getBody().get("Errs");
        if (errs == null || !errs.isArray() || errs.isEmpty()) {
            return;
        }

        var messages = errs.valueStream()
                .filter(node -> !node.isNull())
                .map(JsonNode::asText)
                .toList();
        if (messages.isEmpty()) {
            return;
        }

        throw new PodmanApiException(response.getUri(), response.getStatusCode(), String.join("; ", messages));
    }

    /**
     * Converts a libpod error body into an exception
     *
     * @param body the error body, or {@code null} if it is not in libpod format or could not be read
     * @param fallbackMessage message to use when both {@code message} and {@code cause} are absent
     */
    @NonNull public static PodmanApiException toException(
            @NonNull URI uri, int statusCode, @Nullable JsonNode body, @NonNull String fallbackMessage) {
        var message = text(body, "message");
        var reason = text(body, "cause");
        if (message == null) {
            message = reason == null ? fallbackMessage : reason;
        }

        return new PodmanApiException(uri, statusCode, message, reason);
    }

    @NonNull private static PodmanApiException toException(@NonNull PodmanResponse<JsonNode> response) {
        return toException(response.getUri(), response.getStatusCode(), response.getBody(), UNKNOWN_ERROR);
    }

    @Nullable private static String text(@Nullable JsonNode body, @NonNull String field) {
        if (body == null || !body.isObject()) {
            return null;
        }

        var node = body.get(field);

        return node == null || node.isNull() || node.asText().isBlank() ? null : node.asText();
    }
}
