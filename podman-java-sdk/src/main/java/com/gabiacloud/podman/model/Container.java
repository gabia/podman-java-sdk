/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Container details
 */
@Getter
@Builder
@ToString
@Jacksonized
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Container {

    @JsonProperty("Id")
    private final @NonNull String id;

    @JsonProperty("Name")
    private final @NonNull String name;

    @JsonProperty("State")
    private final @Nullable State state;

    /** Whether it is running */
    public boolean isRunning() {
        return state != null && state.getStatus() == ContainerState.RUNNING;
    }

    /** Health check result. {@link HealthState#NONE} if not configured */
    @NonNull public HealthState getHealth() {
        if (state == null || state.getHealth() == null) {
            return HealthState.NONE;
        }

        return state.getHealth().getStatus();
    }

    /** Container state */
    @Getter
    @Builder
    @ToString
    @Jacksonized
    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class State {

        @JsonProperty("Status")
        private final @NonNull ContainerState status;

        @JsonProperty("Running")
        private final @Nullable Boolean running;

        @JsonProperty("ExitCode")
        private final @Nullable Integer exitCode;

        @JsonProperty("Health")
        private final @Nullable Health health;
    }

    /** Container health check */
    @Getter
    @Builder
    @ToString
    @Jacksonized
    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Health {

        @JsonProperty("Status")
        private final @NonNull HealthState status;

        @JsonProperty("FailingStreak")
        private final @Nullable Integer failingStreak;
    }
}
