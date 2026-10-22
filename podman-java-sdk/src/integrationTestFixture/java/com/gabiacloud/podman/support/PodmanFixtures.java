/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.support;

import com.gabiacloud.podman.api.ContainerApi;
import com.gabiacloud.podman.api.ImageApi;
import com.gabiacloud.podman.api.PodApi;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.dto.ContainerSpec;
import com.gabiacloud.podman.property.PodmanProperty;
import java.io.IOException;
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

    private final PodmanRequestExecutor executor;

    private final RequestBodySerializer serializer;

    private final PodApi pods;

    private final ContainerApi containers;

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
        containers.create(ContainerSpec.builder()
                .name(name + "-sleeper")
                .image(IMAGE)
                .pod(name)
                .command(List.of("sleep", "600"))
                .build());
        pods.start(name);

        return name;
    }

    /** Removes the pod if it exists */
    public void removePod(String name) {
        pods.remove(name, true);
    }

    private void post(String path, Object body) {
        var request = properties
                .newJsonRequest(BASE.resolve(path))
                .POST(publish(body))
                .build();

        executor.processRequired(request);
    }

    private HttpRequest.BodyPublisher publish(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofByteArray(serializer.serialize(body));
        } catch (IOException e) {
            throw new IllegalStateException("cannot serialize fixture body", e);
        }
    }
}
