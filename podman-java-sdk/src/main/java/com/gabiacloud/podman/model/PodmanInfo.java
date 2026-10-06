package com.gabiacloud.podman.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.Nullable;

/**
 * Daemon information ({@code GET /libpod/info})
 */
@Getter
@Builder
@ToString
@Jacksonized
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PodmanInfo {

    private final @Nullable Version version;

    /** Podman version. */
    @Nullable public String getPodmanVersion() {
        return version == null ? null : version.getVersion();
    }

    /** libpod API version. */
    @Nullable public String getApiVersion() {
        return version == null ? null : version.getApiVersion();
    }

    /** {@code version} */
    @Getter
    @Builder
    @ToString
    @Jacksonized
    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Version {

        @JsonProperty("Version")
        private final @Nullable String version;

        @JsonProperty("APIVersion")
        private final @Nullable String apiVersion;

        @JsonProperty("OsArch")
        private final @Nullable String osArch;
    }
}
