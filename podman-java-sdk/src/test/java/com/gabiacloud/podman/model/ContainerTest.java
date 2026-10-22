/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.model;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ContainerTest {

    @Test
    @DisplayName("[Success] Start time is null for a container that never started")
    void neverStarted() {
        var state = Container.State.builder()
                .status(ContainerState.CREATED)
                .startedAt(Instant.parse("0001-01-01T00:00:00Z"))
                .build();

        assertNull(state.getStartedAt());
    }
}
