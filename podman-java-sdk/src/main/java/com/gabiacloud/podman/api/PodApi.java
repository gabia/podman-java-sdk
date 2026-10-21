/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import com.gabiacloud.podman.dto.IdSummary;
import com.gabiacloud.podman.dto.PodActionReport;
import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.model.Pod;
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
 * Pod lookup and lifecycle control
 */
@AllArgsConstructor
public final class PodApi {

    private static final URI BASE = URI.create("/v6.0.0/libpod/pods/");

    private final @NonNull PodmanProperty properties;

    private final @NonNull PodmanRequestExecutor executor;

    private final @NonNull RequestBodySerializer serializer;

    private final @NonNull ResponseBodyConverter converter;

    /**
     * Checks whether a pod exists
     *
     * @return {@code false} instead of an exception if missing
     * @throws PodmanClientException if the connection fails
     */
    public boolean exists(@NonNull String pod) {
        var request = properties.newRequest(path(pod, "exists")).GET().build();

        return !executor.process(request).isNotFound();
    }

    /**
     * Reads pod details
     *
     * @return {@code null} if missing
     * @throws PodmanClientException if the connection fails or the response cannot be read
     * @throws PodmanApiException if the daemon returns an error
     */
    @Nullable public Pod findById(@NonNull String pod) {
        var request = properties.newRequest(path(pod, "json")).GET().build();

        var response = executor.process(request);
        if (response.isNotFound()) {
            return null;
        }

        try {
            return converter.convertValue(response.getBody(), Pod.class);
        } catch (IOException e) {
            throw new PodmanClientException("cannot read pod " + pod, e);
        }
    }

    /**
     * Returns details of all pods
     *
     * @throws PodmanClientException if the connection fails or the response cannot be read
     * @throws PodmanApiException if the daemon returns an error
     */
    @NonNull public List<Pod> findAll() {
        return findAll(Map.of());
    }

    /**
     * Returns details of pods matching the filters
     *
     * @param filters libpod filters, e.g. {@code {"label": ["app=web"]}}
     * @throws PodmanClientException if the connection fails or the response cannot be read
     * @throws PodmanApiException if the daemon returns an error
     */
    @NonNull public List<Pod> findAll(@NonNull Map<String, List<String>> filters) {
        var uri = QueryUrlUtil.addQuery(
                BASE.resolve("json"),
                filters.isEmpty() ? Map.of() : Map.of("filters", QueryUrlUtil.writeFilters(serializer, filters)));
        var request = properties.newRequest(uri).GET().build();

        var response = executor.processRequired(request);

        List<IdSummary> summaries;
        try {
            summaries = converter.convertList(response.getBody(), IdSummary.class);
        } catch (IOException e) {
            throw new PodmanClientException("cannot read the pod list", e);
        }

        return summaries.stream()
                .map(summary -> findById(summary.getId()))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Starts the pod. Does nothing if it is already running
     *
     * @throws PodmanApiException if the pod is missing or a container fails to start
     */
    public void start(@NonNull String pod) {
        operate(pod, "start", Map.of());
    }

    /**
     * Stops the pod. Does nothing if it is already stopped
     *
     * @throws PodmanApiException if the pod is missing or a container fails to stop
     */
    public void stop(@NonNull String pod) {
        operate(pod, "stop", Map.of());
    }

    /**
     * Stops the pod with a grace period. Sends SIGKILL once the grace period elapses
     *
     * @throws PodmanApiException if the pod is missing or a container fails to stop
     */
    public void stop(@NonNull String pod, @NonNull Duration timeout) {
        operate(pod, "stop", Map.of("t", String.valueOf(timeout.toSeconds())));
    }

    /**
     * Restarts the pod
     *
     * @throws PodmanApiException if the pod is missing or a container fails to restart
     */
    public void restart(@NonNull String pod) {
        operate(pod, "restart", Map.of());
    }

    /**
     * Sends a signal to the containers in the pod
     *
     * @param signal e.g. {@code SIGTERM}
     * @throws PodmanApiException if the pod is missing or the signal cannot be sent
     */
    public void kill(@NonNull String pod, @NonNull String signal) {
        operate(pod, "kill", Map.of("signal", signal));
    }

    /**
     * Removes the pod. Does nothing if it is already gone
     *
     * @param force whether to force removal even while running
     * @throws PodmanApiException if the removal fails
     */
    public void remove(@NonNull String pod, boolean force) {
        var uri = QueryUrlUtil.addQuery(BASE.resolve(pod), Map.of("force", String.valueOf(force)));
        var request = properties.newRequest(uri).DELETE().build();

        executor.process(request);
    }

    private void operate(@NonNull String pod, @NonNull String action, @NonNull Map<String, String> query) {
        var uri = QueryUrlUtil.addQuery(path(pod, action), query);
        var request = properties
                .newRequest(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        var response = executor.processRequired(request);
        if (response.getBody().length == 0) {
            return;
        }

        PodActionReport report;
        try {
            report = converter.convertValue(response.getBody(), PodActionReport.class);
        } catch (IOException e) {
            throw new PodmanClientException("cannot read the %s result of pod %s".formatted(action, pod), e);
        }

        var errors = report.joinErrors();
        if (errors != null) {
            throw new PodmanApiException(response.getUri(), response.getStatusCode(), errors);
        }
    }

    @NonNull private URI path(@NonNull String pod, @NonNull String action) {
        return BASE.resolve(pod + "/" + action);
    }
}
