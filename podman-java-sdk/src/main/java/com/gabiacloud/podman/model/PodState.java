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
 * Pod status
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PodState {
    CREATED("Created"),
    RUNNING("Running"),
    DEGRADED("Degraded"),
    STOPPED("Stopped"),
    EXITED("Exited"),
    PAUSED("Paused"),
    DEAD("Dead"),
    ERROR("Error"),
    UNKNOWN("");

    @JsonValue
    private final @NonNull String value;

    /** Status for a libpod value. Unknown values map to {@link #UNKNOWN} */
    @JsonCreator
    @NonNull public static PodState from(@Nullable String value) {
        return Arrays.stream(values())
                .filter(state -> state != UNKNOWN && state.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
