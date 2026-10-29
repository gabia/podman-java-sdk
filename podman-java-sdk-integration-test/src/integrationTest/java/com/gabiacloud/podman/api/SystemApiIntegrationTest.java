/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gabiacloud.podman.annotation.PodmanTest;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.dto.PodmanEventQuery;
import com.gabiacloud.podman.model.PodmanEventType;
import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.support.PodmanFixtures;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @Nested
    final class Events {

        private SystemApi api;

        private PodmanProperty properties;

        private PodmanRequestExecutor executor;

        private PodmanFixtures fixtures;

        @BeforeEach
        void beforeEach(
                SystemApi api, PodmanProperty properties, PodmanRequestExecutor executor, PodmanFixtures fixtures) {
            this.api = api;
            this.properties = properties;
            this.executor = executor;
            this.fixtures = fixtures;
        }

        @Test
        @DisplayName("[Success] Receives the start event of a pod")
        void receivesPodStartEvent() throws Exception {
            var name = "events-" + UUID.randomUUID();
            var query = PodmanEventQuery.builder()
                    .since(Instant.now().minusSeconds(1))
                    .filters(Map.of("type", List.of("pod"), "event", List.of("start")))
                    .build();

            try (var stream = api.events(query)) {
                fixtures.startPod(name, "events");

                var event = assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
                    var received = stream.next();
                    while (received != null && !name.equals(received.getName())) {
                        received = stream.next();
                    }
                    return received;
                });

                assertNotNull(event);
                assertTrue(event.getType() == PodmanEventType.POD);
                assertTrue("start".equals(event.getAction()));
            } finally {
                fixtures.removePod(name);
            }
        }

        @Test
        @DisplayName("[Success] Stream outlives the request timeout")
        void streamOutlivesTimeout() throws Exception {
            var until = Instant.now().plusSeconds(4);
            var uri = URI.create("/v6.0.0/libpod/events?stream=true&until=" + until.getEpochSecond());
            var request = properties
                    .newRequest(uri)
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            var started = Instant.now();
            try (var body = executor.stream(request).getBody()) {
                body.readAllBytes();
            }

            assertTrue(Duration.between(started, Instant.now()).toMillis() > 2_500);
        }
    }
}
