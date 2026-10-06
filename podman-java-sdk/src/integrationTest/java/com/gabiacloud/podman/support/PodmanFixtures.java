/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.api.ImageApi;
import com.gabiacloud.podman.api.PodApi;
import com.gabiacloud.podman.component.JacksonBodyPublisher;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.property.PodmanProperty;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;

/**
 * Creates and removes pods for integration tests
 */
@AllArgsConstructor
public final class PodmanFixtures {

    /** Lightweight fixture image for long-running containers */
    public static final String IMAGE = "docker.io/library/alpine:3";

    public static final String TEST_ID_LABEL = "com.gabiacloud.podman.test-id";

    private static final URI BASE = URI.create("/v6.0.0/libpod/");

    private final PodmanProperty properties;

    private final ObjectMapper mapper;

    private final PodmanRequestExecutor executor;

    private final PodApi pods;

    private final ImageApi images;

    /** Pulls the fixture image if it is missing */
    public void ensureImage() {
        if (!images.exists(IMAGE)) {
            images.pull(IMAGE);
        }
    }

    /**
     * Creates and starts a pod with a single container
     *
     * @return name of the created pod
     */
    public String startPod(String name, String testId) {
        ensureImage();
        removePod(name);

        post("pods/create", Map.of("name", name, "labels", Map.of(TEST_ID_LABEL, testId)));
        post(
                "containers/create",
                Map.of("name", name + "-sleeper", "image", IMAGE, "pod", name, "command", List.of("sleep", "600")));
        pods.start(name);

        return name;
    }

    /** Removes the pod if it exists */
    public void removePod(String name) {
        pods.remove(name, true);
    }

    private JsonNode post(String path, Object body) {
        var request = properties
                .newJsonRequest(BASE.resolve(path))
                .POST(publish(body))
                .build();

        var response = executor.process(request);
        if (response.getStatusCode() >= 400) {
            throw new IllegalStateException(
                    "cannot prepare fixture %s: %d %s".formatted(path, response.getStatusCode(), response.getBody()));
        }

        return response.getBody();
    }

    private HttpRequest.BodyPublisher publish(Object body) {
        try {
            return new JacksonBodyPublisher(mapper, body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cannot serialize fixture body", e);
        }
    }
}
