/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Conditions for subscribing to daemon events
 */
@Getter
@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PodmanEventQuery {

    /** Replays events from this time before streaming new ones */
    private final @Nullable Instant since;

    /** Ends the stream at this time */
    private final @Nullable Instant until;

    /** libpod filters, e.g. {@code {"type": ["container"], "event": ["die"]}} */
    @Builder.Default
    private final @NonNull Map<String, List<String>> filters = Map.of();
}
