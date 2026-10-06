/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Converts an object into a JSON request body
 */
public final class JacksonBodyPublisher implements HttpRequest.BodyPublisher {

    private final HttpRequest.@NonNull BodyPublisher upstream;

    /**
     * @throws JsonProcessingException if serialization fails
     */
    public JacksonBodyPublisher(@NonNull ObjectMapper mapper, @Nullable Object body) throws JsonProcessingException {
        this.upstream = HttpRequest.BodyPublishers.ofByteArray(mapper.writeValueAsBytes(body));
    }

    @Override
    public long contentLength() {
        return upstream.contentLength();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        upstream.subscribe(subscriber);
    }
}
