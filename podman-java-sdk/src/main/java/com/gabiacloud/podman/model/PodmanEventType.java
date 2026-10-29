/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Kind of object an event is about
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PodmanEventType {
    CONTAINER("container"),
    POD("pod"),
    IMAGE("image"),
    VOLUME("volume"),
    NETWORK("network"),
    SYSTEM("system"),
    UNKNOWN("");

    @JsonValue
    private final @NonNull String value;

    /** Type for a libpod value. Unknown values map to {@link #UNKNOWN} */
    @JsonCreator
    @NonNull public static PodmanEventType from(@Nullable String value) {
        return Arrays.stream(values())
                .filter(type -> type != UNKNOWN && type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
