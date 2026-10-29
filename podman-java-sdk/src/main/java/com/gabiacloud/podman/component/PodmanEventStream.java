/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.model.PodmanEvent;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Stream of daemon events that stays open until it is closed
 */
public final class PodmanEventStream implements Closeable {

    private final @NonNull URI uri;

    private final @NonNull ResponseBodyConverter converter;

    private final @NonNull InputStream body;

    private final @NonNull BufferedReader reader;

    private volatile boolean closed;

    public PodmanEventStream(@NonNull URI uri, @NonNull InputStream body, @NonNull ResponseBodyConverter converter) {
        this.uri = uri;
        this.converter = converter;
        this.body = body;
        this.reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8));
    }

    /**
     * Waits for the next event
     *
     * @return {@code null} once the stream has ended or been closed
     * @throws PodmanClientException if the connection breaks or an event cannot be read
     */
    @Nullable public PodmanEvent next() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    return converter.convertValue(line.getBytes(StandardCharsets.UTF_8), PodmanEvent.class);
                }
            }

            return null;
        } catch (IOException e) {
            if (closed) {
                return null;
            }

            throw new PodmanClientException("cannot read events from " + uri, e);
        }
    }

    /** Ends the subscription. Also unblocks a thread waiting in {@link #next()} */
    @Override
    public void close() throws IOException {
        closed = true;
        body.close();
    }
}
