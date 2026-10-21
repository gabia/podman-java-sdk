/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import com.gabiacloud.podman.property.PodmanProperty;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnixDomainSocketAddress;
import java.net.http.HttpRequest;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * Podman request processor over a Unix socket
 */
@AllArgsConstructor
public final class UnixSocketPodmanRequestProcessor implements PodmanRequestProcessor {

    private final @NonNull PodmanProperty properties;

    @Override
    @NonNull public PodmanResponse<byte[]> apply(@NonNull HttpRequest request) throws IOException, InterruptedException {
        try (var channel = open()) {
            var raw = UnixHttpExchange.send(channel, request, body(request));

            return response(request, raw, raw.readBody());
        }
    }

    @Override
    @NonNull public PodmanResponse<InputStream> stream(@NonNull HttpRequest request) throws IOException, InterruptedException {
        var channel = open();
        try {
            var raw = UnixHttpExchange.send(channel, request, body(request));

            return response(request, raw, closingStream(raw.getBody(), channel));
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
    }

    @NonNull private DeadlineChannel open() throws IOException {
        return DeadlineChannel.connect(UnixDomainSocketAddress.of(properties.socketPath()), properties.getTimeout());
    }

    private byte[] body(@NonNull HttpRequest request) throws IOException, InterruptedException {
        return RequestBodyCollector.collect(request.bodyPublisher().orElse(null), properties.getTimeout());
    }

    @NonNull private static <T> PodmanResponse<T> response(
            @NonNull HttpRequest request, UnixHttpExchange.@NonNull RawResponse raw, T body) {
        return new PodmanResponse<>(raw.getStatusCode(), request.uri(), body);
    }

    @NonNull private static InputStream closingStream(@NonNull InputStream body, @NonNull DeadlineChannel channel) {
        return new FilterInputStream(body) {

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    channel.close();
                }
            }
        };
    }
}
