/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gabiacloud.podman.annotation.PodmanTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@PodmanTest
final class SystemApiIntegrationTest {

    @Nested
    final class Connection {

        private SystemApi api;

        @BeforeEach
        void beforeEach(SystemApi api) {
            this.api = api;
        }

        @Test
        @DisplayName("[Success] Daemon responds")
        void pingsTheDaemon() {
            assertDoesNotThrow(api::ping);
        }

        @Test
        @DisplayName("[Success] Reads the version")
        void readsVersion() {
            var info = api.info();

            assertNotNull(info.getPodmanVersion());
            assertNotNull(info.getApiVersion());
            assertTrue(info.getPodmanVersion().contains("."));
        }
    }
}
