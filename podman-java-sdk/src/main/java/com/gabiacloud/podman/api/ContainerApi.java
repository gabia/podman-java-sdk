/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import com.gabiacloud.podman.dto.ContainerSpec;
import com.gabiacloud.podman.dto.IdSummary;
import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.model.Container;
import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.util.QueryUrlUtil;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Container lookup and lifecycle control
 */
@AllArgsConstructor
public final class ContainerApi {

    private static final URI BASE = URI.create("/v6.0.0/libpod/containers/");

    private final @NonNull PodmanProperty properties;

    private final @NonNull PodmanRequestExecutor executor;

    private final @NonNull RequestBodySerializer serializer;

    private final @NonNull ResponseBodyConverter converter;

    /**
     * Checks whether a container exists
     *
     * @return {@code false} instead of an exception if missing
     * @throws PodmanClientException if the connection fails
     */
    public boolean exists(@NonNull String container) {
        var request = properties.newRequest(path(container, "exists")).GET().build();

        return !executor.process(request).isNotFound();
    }

    /**
     * Reads container details
     *
     * @return {@code null} if missing
     * @throws PodmanClientException if the connection fails or the response cannot be read
     * @throws PodmanApiException if the daemon returns an error
     */
    @Nullable public Container findById(@NonNull String container) {
        var request = properties.newRequest(path(container, "json")).GET().build();

        var response = executor.process(request);
        if (response.isNotFound()) {
            return null;
        }

        try {
            return converter.convertValue(response.getBody(), Container.class);
        } catch (IOException e) {
            throw new PodmanClientException("cannot read container " + container, e);
        }
    }

    /**
     * Returns details of all containers, including stopped ones
     *
     * @throws PodmanClientException if the connection fails or the response cannot be read
     * @throws PodmanApiException if the daemon returns an error
     */
    @NonNull public List<Container> findAll() {
        return findAll(Map.of());
    }

    /**
     * Returns details of containers matching the filters
     *
     * @param filters libpod filters, e.g. {@code {"pod": ["my-pod"]}}
     * @throws PodmanClientException if the connection fails or the response cannot be read
     * @throws PodmanApiException if the daemon returns an error
     */
    @NonNull public List<Container> findAll(@NonNull Map<String, List<String>> filters) {
        var query = filters.isEmpty()
                ? Map.of("all", "true")
                : Map.of("all", "true", "filters", QueryUrlUtil.writeFilters(serializer, filters));

        var request = properties
                .newRequest(QueryUrlUtil.addQuery(BASE.resolve("json"), query))
                .GET()
                .build();

        var response = executor.processRequired(request);

        List<IdSummary> summaries;
        try {
            summaries = converter.convertList(response.getBody(), IdSummary.class);
        } catch (IOException e) {
            throw new PodmanClientException("cannot read the container list", e);
        }

        return summaries.stream()
                .map(summary -> findById(summary.getId()))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Creates a container without starting it
     *
     * @return ID of the created container
     * @throws PodmanClientException if the connection fails or the request or response cannot be processed
     * @throws PodmanApiException if the image is missing or the creation fails
     */
    @NonNull public String create(@NonNull ContainerSpec spec) {
        byte[] body;
        try {
            body = serializer.serialize(spec);
        } catch (IOException e) {
            throw new PodmanClientException("cannot write the spec of container " + spec.getName(), e);
        }

        var request = properties
                .newJsonRequest(BASE.resolve("create"))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        var response = executor.processRequired(request);

        try {
            return converter.convertValue(response.getBody(), IdSummary.class).getId();
        } catch (IOException e) {
            throw new PodmanClientException("cannot read the creation result of container " + spec.getName(), e);
        }
    }

    /**
     * Starts the container. Does nothing if it is already running
     *
     * @throws PodmanClientException if the connection fails
     * @throws PodmanApiException if the container is missing or fails to start
     */
    public void start(@NonNull String container) {
        var request = properties
                .newRequest(path(container, "start"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        executor.processRequired(request);
    }

    /**
     * Waits until the container stops, within the default timeout
     *
     * @return exit code of the container
     * @throws PodmanClientException if the connection fails, the timeout elapses, or the response cannot be read
     * @throws PodmanApiException if the container is missing or the wait fails
     */
    public int wait(@NonNull String container) {
        return wait(container, properties.getTimeout());
    }

    /**
     * Waits until the container stops
     *
     * @param timeout how long to wait for the container to stop
     * @return exit code of the container
     * @throws PodmanClientException if the connection fails, the timeout elapses, or the response cannot be read
     * @throws PodmanApiException if the container is missing or the wait fails
     */
    public int wait(@NonNull String container, @NonNull Duration timeout) {
        var uri = QueryUrlUtil.addQuery(path(container, "wait"), Map.of("condition", "stopped"));
        var request = properties
                .newRequest(uri)
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        var response = executor.processRequired(request);

        try {
            return converter.convertValue(response.getBody(), Integer.class);
        } catch (IOException e) {
            throw new PodmanClientException("cannot read the exit code of container " + container, e);
        }
    }

    @NonNull private URI path(@NonNull String container, @NonNull String action) {
        return BASE.resolve(container + "/" + action);
    }
}
