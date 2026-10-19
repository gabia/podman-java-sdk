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
import org.jspecify.annotations.Nullable;

/**
 * libpod error body
 */
@Getter
@Builder
@ToString
@Jacksonized
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PodmanError {

    @JsonProperty("message")
    private final @Nullable String message;

    /** {@code cause} */
    @JsonProperty("cause")
    private final @Nullable String reason;
}
