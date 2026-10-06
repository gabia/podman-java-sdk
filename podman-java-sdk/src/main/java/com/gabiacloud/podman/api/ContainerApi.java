/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.model.Container;
import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.util.PodmanResponseUtil;
import com.gabiacloud.podman.util.QueryUrlUtil;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Container lookup
 */
@AllArgsConstructor
public final class ContainerApi {

    private static final URI BASE = URI.create("/v6.0.0/libpod/containers/");

    private final @NonNull PodmanProperty properties;

    private final @NonNull ObjectMapper mapper;

    private final @NonNull PodmanRequestExecutor executor;

    /**
     * Checks whether a container exists
     *
     * @return {@code false} instead of an exception if missing
     * @throws PodmanClientException if the connection fails
     */
    public boolean exists(@NonNull String container) {
        var request = properties.newRequest(path(container, "exists")).GET().build();

        return PodmanResponseUtil.exists(executor.process(request));
    }

    /**
     * Reads container details
     *
     * @return {@code null} if missing
     * @throws PodmanClientException if the connection fails
     * @throws PodmanApiException if the daemon returns an error
     */
    @Nullable public Container findById(@NonNull String container) {
        var request = properties.newRequest(path(container, "json")).GET().build();

        return PodmanResponseUtil.findOne(mapper, executor.process(request), Container.class);
    }

    /**
     * Returns details of all containers, including stopped ones
     *
     * @throws PodmanClientException if the connection fails
     * @throws PodmanApiException if the daemon returns an error
     */
    @NonNull public List<Container> findAll() {
        return findAll(Map.of());
    }

    /**
     * Returns details of containers matching the filters
     *
     * @param filters libpod filters, e.g. {@code {"pod": ["my-pod"]}}
     * @throws PodmanClientException if the connection fails
     * @throws PodmanApiException if the daemon returns an error
     */
    @NonNull public List<Container> findAll(@NonNull Map<String, List<String>> filters) {
        var query = filters.isEmpty()
                ? Map.of("all", "true")
                : Map.of("all", "true", "filters", QueryUrlUtil.writeFilters(mapper, filters));

        var request = properties
                .newRequest(QueryUrlUtil.addQuery(BASE.resolve("json"), query))
                .GET()
                .build();

        var response = executor.process(request);
        PodmanResponseUtil.error404(response);

        return response.getBody()
                .valueStream()
                .map(summary -> findById(summary.get("Id").asText()))
                .filter(Objects::nonNull)
                .toList();
    }

    @NonNull private URI path(@NonNull String container, @NonNull String action) {
        return BASE.resolve(container + "/" + action);
    }
}
