/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import com.gabiacloud.podman.component.PodmanEventStream;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import com.gabiacloud.podman.dto.PodmanEventQuery;
import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.model.PodmanInfo;
import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.util.QueryUrlUtil;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * Checks daemon connectivity and retrieves information
 */
@AllArgsConstructor
public final class SystemApi {

    private static final URI PING_PATH = URI.create("/_ping");

    private static final URI INFO_PATH = URI.create("/v6.0.0/libpod/info");

    private static final URI EVENTS_PATH = URI.create("/v6.0.0/libpod/events");

    private final @NonNull PodmanProperty properties;

    private final @NonNull PodmanRequestExecutor executor;

    private final @NonNull RequestBodySerializer serializer;

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

    /**
     * Subscribes to daemon events. The stream stays open until it is closed, so the caller must close it
     *
     * @throws PodmanClientException if the connection fails or the query cannot be written
     * @throws PodmanApiException if the daemon returns an error response
     */
    @NonNull public PodmanEventStream events(@NonNull PodmanEventQuery query) {
        var params = new LinkedHashMap<String, String>();
        params.put("stream", "true");
        if (query.getSince() != null) {
            params.put("since", toEpochSeconds(query.getSince()));
        }
        if (query.getUntil() != null) {
            params.put("until", toEpochSeconds(query.getUntil()));
        }
        if (!query.getFilters().isEmpty()) {
            params.put("filters", QueryUrlUtil.writeFilters(serializer, query.getFilters()));
        }

        var request = properties
                .newRequest(QueryUrlUtil.addQuery(EVENTS_PATH, params))
                .GET()
                .build();

        var response = executor.stream(request);

        return new PodmanEventStream(request.uri(), response.getBody(), converter);
    }

    @NonNull private static String toEpochSeconds(@NonNull Instant instant) {
        return "%d.%09d".formatted(instant.getEpochSecond(), instant.getNano());
    }
}
