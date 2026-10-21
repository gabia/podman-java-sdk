/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.spring.boot.autoconfigure;

import com.gabiacloud.podman.property.PodmanProperty;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Podman connection settings bound from the {@code podman} prefix
 */
@Getter
@ConfigurationProperties(prefix = "podman")
public class PodmanSpringProperties {

    /** Podman endpoint, either a unix socket such as unix:///run/podman/podman.sock or tcp such as tcp://127.0.0.1:8080 */
    private final @NonNull URI host;

    /** Request timeout */
    private final @NonNull Duration timeout;

    /** Falls back to the SDK defaults for settings that are not given */
    public PodmanSpringProperties(@Nullable URI host, @Nullable Duration timeout) {
        this.host = Objects.requireNonNullElse(host, PodmanProperty.DEFAULT_HOST);
        this.timeout = Objects.requireNonNullElse(timeout, PodmanProperty.DEFAULT_TIMEOUT);
    }
}
