/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.Nullable;

/**
 * Result of a pod operation such as start or stop
 */
@Getter
@Builder
@ToString
@Jacksonized
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PodActionReport {

    /** Per-container errors */
    @JsonProperty("Errs")
    private final @Nullable List<@Nullable String> errs;

    /**
     * Joins the per-container errors
     *
     * @return {@code null} if there are no errors other than {@code null}
     */
    @Nullable public String joinErrors() {
        if (errs == null) {
            return null;
        }

        var messages = errs.stream().filter(Objects::nonNull).toList();

        return messages.isEmpty() ? null : String.join("; ", messages);
    }
}
