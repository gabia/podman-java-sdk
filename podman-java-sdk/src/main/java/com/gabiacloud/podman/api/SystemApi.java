/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.model.PodmanInfo;
import com.gabiacloud.podman.property.PodmanProperty;
import java.io.IOException;
import java.net.URI;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * Checks daemon connectivity and retrieves information
 */
@AllArgsConstructor
public final class SystemApi {

    private static final URI PING_PATH = URI.create("/_ping");

    private static final URI INFO_PATH = URI.create("/v6.0.0/libpod/info");

    private final @NonNull PodmanProperty properties;

    private final @NonNull PodmanRequestExecutor executor;

    private final @NonNull ResponseBodyConverter converter;

    /**
     * Checks whether the daemon responds
     *
     * @throws PodmanClientException if the connection fails
     * @throws PodmanApiException if the daemon returns an error response
     */
    public void ping() {
        var request = properties.newRequest(PING_PATH).GET().build();

        executor.processRequired(request);
    }

    /**
     * Reads daemon information
     *
     * @throws PodmanClientException if the connection fails or the response cannot be read
     * @throws PodmanApiException if the daemon returns an error response
     */
    @NonNull public PodmanInfo info() {
        var request = properties.newRequest(INFO_PATH).GET().build();

        var response = executor.processRequired(request);

        try {
            return converter.convertValue(response.getBody(), PodmanInfo.class);
        } catch (IOException e) {
            throw new PodmanClientException("cannot read the daemon information", e);
        }
    }
}
