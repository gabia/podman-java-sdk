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
 * Result of a container health check
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum HealthState {
    HEALTHY("healthy"),
    UNHEALTHY("unhealthy"),
    STARTING("starting"),
    STOPPED("stopped"),
    NONE("");

    @JsonValue
    private final @NonNull String value;

    /** Status for a libpod value. Empty and unknown values map to {@link #NONE} */
    @JsonCreator
    @NonNull public static HealthState from(@Nullable String value) {
        return Arrays.stream(values())
                .filter(state -> state != NONE && state.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(NONE);
    }
}
