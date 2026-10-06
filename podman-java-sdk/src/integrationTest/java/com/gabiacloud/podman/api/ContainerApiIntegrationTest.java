package com.gabiacloud.podman.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gabiacloud.podman.annotation.PodmanTest;
import com.gabiacloud.podman.model.ContainerState;
import com.gabiacloud.podman.model.HealthState;
import com.gabiacloud.podman.support.PodmanFixtures;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@PodmanTest
final class ContainerApiIntegrationTest {

    private static final String POD = "podman-java-sdk-integration-test-container";

    private static final String CONTAINER = POD + "-sleeper";

    private static final String MISSING = "podman-java-sdk-integration-test-missing";

    private static final String TEST_ID = "9002";

    private ContainerApi api;

    private PodmanFixtures fixtures;

    @BeforeEach
    void beforeEach(ContainerApi api, PodmanFixtures fixtures) {
        this.api = api;
        this.fixtures = fixtures;
        fixtures.startPod(POD, TEST_ID);
    }

    @AfterEach
    void afterEach() {
        fixtures.removePod(POD);
    }

    @Test
    @DisplayName("[Success] Finds a running container")
    void readsRunningContainer() {
        assertTrue(api.exists(CONTAINER));

        var container = api.findById(CONTAINER);
        assertNotNull(container);
        assertEquals(ContainerState.RUNNING, container.getState().getStatus());
        assertTrue(container.isRunning());
        assertEquals(HealthState.NONE, container.getHealth());
    }

    @Test
    @DisplayName("[Success] Returns false and null for a missing container")
    void missingContainer() {
        assertFalse(api.exists(MISSING));
        assertNull(api.findById(MISSING));
    }

    @Test
    @DisplayName("[Success] Filters containers by pod")
    void filtersByPod() {
        var containers = api.findAll(Map.of("pod", List.of(POD)));

        assertTrue(containers.stream().anyMatch(container -> CONTAINER.equals(container.getName())));
    }
}
