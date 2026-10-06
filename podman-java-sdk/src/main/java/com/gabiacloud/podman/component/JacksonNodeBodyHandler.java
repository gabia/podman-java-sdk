/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * Reads a JSON response body
 */
@AllArgsConstructor
public final class JacksonNodeBodyHandler implements HttpResponse.BodyHandler<JsonNode> {

    private final @NonNull ObjectMapper mapper;

    @Override
    public HttpResponse.BodySubscriber<JsonNode> apply(HttpResponse.ResponseInfo responseInfo) {
        var upstream = HttpResponse.BodySubscribers.ofInputStream();

        return HttpResponse.BodySubscribers.mapping(upstream, this::readTree);
    }

    @NonNull private JsonNode readTree(@NonNull InputStream stream) {
        try (stream) {
            var bytes = stream.readAllBytes();
            if (bytes.length == 0) {
                return MissingNode.getInstance();
            }

            return mapper.readTree(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
