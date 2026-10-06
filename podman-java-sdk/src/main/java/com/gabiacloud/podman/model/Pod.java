package com.gabiacloud.podman.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NonNull;

/**
 * Pod details
 */
@Getter
@Builder
@ToString
@Jacksonized
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Pod {

    @JsonProperty("Id")
    private final @NonNull String id;

    @JsonProperty("Name")
    private final @NonNull String name;

    @JsonProperty("State")
    private final @NonNull PodState state;

    /** Creation time, as the raw string returned by libpod */
    @JsonProperty("Created")
    private final @NonNull String created;

    @JsonProperty("NumContainers")
    private final int numContainers;

    @Singular
    @JsonProperty("Labels")
    private final @NonNull Map<String, String> labels;

    @Singular("container")
    @JsonProperty("Containers")
    private final @NonNull List<PodContainer> containers;

    /** Whether it is running */
    public boolean isRunning() {
        return state == PodState.RUNNING;
    }

    /** Container in the pod */
    @Getter
    @Builder
    @ToString
    @Jacksonized
    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class PodContainer {

        @JsonProperty("Id")
        private final @NonNull String id;

        @JsonProperty("Name")
        private final @NonNull String name;

        @JsonProperty("State")
        private final @NonNull ContainerState state;
    }
}
