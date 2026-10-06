package com.gabiacloud.podman.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gabiacloud.podman.annotation.PodmanTest;
import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.support.PodmanFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@PodmanTest
final class ImageApiIntegrationTest {

    private ImageApi api;

    private PodmanFixtures fixtures;

    @BeforeEach
    void beforeEach(ImageApi api, PodmanFixtures fixtures) {
        this.api = api;
        this.fixtures = fixtures;
    }

    @Test
    @DisplayName("[Success] Image exists after pull")
    void pulledImageExists() {
        fixtures.ensureImage();

        assertTrue(api.exists(PodmanFixtures.IMAGE));
    }

    @Test
    @DisplayName("[Success] Returns false for a missing image")
    void missingImageIsFalse() {
        assertFalse(api.exists("docker.io/library/nothing-here:0"));
    }

    @Test
    @DisplayName("[Failure] Treat a 200 response for a missing tag as an error")
    void failedPullIsAnException() {
        assertThrows(PodmanApiException.class, () -> api.pull("docker.io/library/alpine:no-such-tag-9001"));
    }
}
