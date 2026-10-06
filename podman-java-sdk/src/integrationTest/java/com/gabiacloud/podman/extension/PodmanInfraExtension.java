/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.extension;

import com.gabiacloud.podman.property.PodmanProperty;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.ComposeContainer;

/**
 * Prepares the Podman environment that integration tests connect to
 */
public final class PodmanInfraExtension implements BeforeAllCallback {

    static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PodmanInfraExtension.class);

    private static final String SERVICE = "podman";

    private static final int PORT = 8080;

    private static final ComposeContainer CONTAINERS = new ComposeContainer(
                    new File("src/integrationTest/resources/docker-compose.test.yml"))
            .withExposedService(SERVICE, PORT)
            .withStartupTimeout(Duration.ofSeconds(180));

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getRoot().getStore(NAMESPACE);
        if (store.get(PodmanProperty.class) != null) {
            return;
        }

        store.put(PodmanProperty.class, resolve());
    }

    private static PodmanProperty resolve() {
        if (Boolean.parseBoolean(DotenvExtension.get("USE_EXTERNAL_PODMAN"))) {
            return PodmanProperty.builder()
                    .host(URI.create(DotenvExtension.get("TEST_PODMAN_URI")))
                    .timeout(PodmanProperty.DEFAULT_TIMEOUT)
                    .build();
        }

        CONTAINERS.start();
        var host = CONTAINERS.getServiceHost(SERVICE, PORT);
        var port = CONTAINERS.getServicePort(SERVICE, PORT);

        return PodmanProperty.builder()
                .host(URI.create("tcp://%s:%d".formatted(host, port)))
                .timeout(PodmanProperty.DEFAULT_TIMEOUT)
                .build();
    }
}
