/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import com.gabiacloud.podman.property.PodmanProperty;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import org.jspecify.annotations.NonNull;

/**
 * Layer that sends requests to Podman
 */
public interface PodmanRequestProcessor {

    /** Creates a request processor that matches the host setting */
    @NonNull static PodmanRequestProcessor forHost(@NonNull PodmanProperty properties) {
        return properties.isUnixSocket()
                ? new UnixSocketPodmanRequestProcessor(properties)
                : new HttpClientPodmanRequestProcessor();
    }

    /**
     * Sends a request and reads the whole body
     *
     * @param request request to send
     * @return response carrying the status code as is
     * @throws IOException if the exchange fails
     * @throws InterruptedException if interrupted while waiting
     */
    @NonNull PodmanResponse<byte[]> apply(@NonNull HttpRequest request) throws IOException, InterruptedException;

    /**
     * Sends a request and reads the body as a raw stream
     *
     * @throws IOException if the exchange fails
     * @throws InterruptedException if interrupted while waiting
     */
    @NonNull PodmanResponse<InputStream> stream(@NonNull HttpRequest request) throws IOException, InterruptedException;
}
