package com.gabiacloud.podman.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gabiacloud.podman.annotation.PodmanTest;
import com.gabiacloud.podman.model.PodState;
import com.gabiacloud.podman.support.PodmanFixtures;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@PodmanTest
final class PodApiIntegrationTest {

    private static final String POD = "podman-java-sdk-integration-test";

    private static final String TEST_ID = "9001";

    private static final String MISSING = "podman-java-sdk-integration-test-missing";

    @Nested
    @DisplayName("Missing pod")
    final class Missing {

        private PodApi api;

        @BeforeEach
        void beforeEach(PodApi api) {
            this.api = api;
        }

        @Test
        @DisplayName("[Success] exists returns false and findById returns null for a missing pod")
        void existsIsFalse() {
            assertFalse(api.exists(MISSING));
            assertNull(api.findById(MISSING));
        }

        @Test
        @DisplayName("[Success] Remove a missing pod")
        void removeIsIdempotent() {
            assertDoesNotThrow(() -> api.remove(MISSING, true));
        }
    }

    @Nested
    @DisplayName("Lifecycle control")
    final class Lifecycle {

        private PodApi api;

        private PodmanFixtures fixtures;

        @BeforeEach
        void beforeEach(PodApi api, PodmanFixtures fixtures) {
            this.api = api;
            this.fixtures = fixtures;
            fixtures.startPod(POD, TEST_ID);
        }

        @AfterEach
        void afterEach() {
            fixtures.removePod(POD);
        }

        @Test
        @DisplayName("[Success] Pod is running after start")
        void startedPodIsRunning() {
            var pod = api.findById(POD);

            assertNotNull(pod);
            assertEquals(PodState.RUNNING, pod.getState());
            assertTrue(pod.isRunning());
            assertEquals(TEST_ID, pod.getLabels().get(PodmanFixtures.TEST_ID_LABEL));
        }

        @Test
        @DisplayName("[Success] Starting a running pod is idempotent")
        void startIsIdempotent() {
            assertDoesNotThrow(() -> api.start(POD));
        }

        @Test
        @DisplayName("[Success] Pod is no longer running after stop")
        void stoppedPodIsExited() {
            api.stop(POD);

            var pod = api.findById(POD);
            assertNotNull(pod);
            assertFalse(pod.isRunning());
        }

        @Test
        @DisplayName("[Success] Pod is running after restart")
        void restartBringsItBack() {
            api.restart(POD);

            var pod = api.findById(POD);
            assertNotNull(pod);
            assertEquals(PodState.RUNNING, pod.getState());
        }
    }

    @Nested
    @DisplayName("List")
    final class FindAll {

        private PodApi api;

        private PodmanFixtures fixtures;

        @BeforeEach
        void beforeEach(PodApi api, PodmanFixtures fixtures) {
            this.api = api;
            this.fixtures = fixtures;
            fixtures.startPod(POD, TEST_ID);
        }

        @AfterEach
        void afterEach() {
            fixtures.removePod(POD);
        }

        @Test
        @DisplayName("[Success] A label-filtered list contains populated detail fields")
        void fillsInspectFields() {
            var pods = api.findAll(Map.of("label", List.of(PodmanFixtures.TEST_ID_LABEL + "=" + TEST_ID)));

            assertEquals(1, pods.size());
            var pod = pods.getFirst();
            assertTrue(pod.isRunning());
            assertEquals(TEST_ID, pod.getLabels().get(PodmanFixtures.TEST_ID_LABEL));
            assertEquals(pod.getContainers().size(), pod.getNumContainers());
            assertTrue(
                    pod.getContainers().stream().anyMatch(container -> (POD + "-sleeper").equals(container.getName())));
        }

        @Test
        @DisplayName("[Success] Stopped pods are included in the list")
        void includesStopped() {
            api.stop(POD);

            var pods = api.findAll(Map.of("label", List.of(PodmanFixtures.TEST_ID_LABEL + "=" + TEST_ID)));

            assertEquals(1, pods.size());
            assertFalse(pods.getFirst().isRunning());
        }
    }
}
