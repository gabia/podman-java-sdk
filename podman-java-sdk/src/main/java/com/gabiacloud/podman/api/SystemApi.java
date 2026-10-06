/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.model.PodmanInfo;
import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.util.PodmanResponseUtil;
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

    private final @NonNull ObjectMapper mapper;

    private final @NonNull PodmanRequestExecutor executor;

    /**
     * Checks whether the daemon responds
     *
     * @throws PodmanClientException if the connection fails
     * @throws PodmanApiException if the daemon returns an error response
     */
    public void ping() {
        var request = properties.newRequest(PING_PATH).GET().build();

        var response = executor.stream(request);
        try (var body = response.getBody()) {
            body.readAllBytes();
        } catch (IOException e) {
            throw new PodmanClientException("cannot read the ping response", e);
        }

        if (response.getStatusCode() >= 400) {
            throw new PodmanApiException(request.uri(), response.getStatusCode(), "podman did not answer the ping");
        }
    }

    /**
     * Reads daemon information
     *
     * @throws PodmanClientException if the connection fails
     * @throws PodmanApiException if the daemon returns an error response
     */
    @NonNull public PodmanInfo info() {
        var request = properties.newRequest(INFO_PATH).GET().build();

        var response = executor.process(request);
        PodmanResponseUtil.error404(response);

        return mapper.convertValue(response.getBody(), PodmanInfo.class);
    }
}
