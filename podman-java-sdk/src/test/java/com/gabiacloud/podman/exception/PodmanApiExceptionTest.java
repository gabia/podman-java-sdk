/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gabiacloud.podman.model.PodmanError;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class PodmanApiExceptionTest {

    private static final URI PULL = URI.create("http://d/v6.0.0/libpod/images/pull");

    @Test
    @DisplayName("[Success] Uses message when available")
    void prefersMessage() {
        var error = PodmanError.builder()
                .message("manifest unknown")
                .reason("not found")
                .build();

        var thrown = PodmanApiException.of(PULL, 404, error, "fallback");

        assertTrue(thrown.getMessage().contains("manifest unknown"));
        assertEquals("not found", thrown.getReason());
    }

    @Test
    @DisplayName("[Success] Falls back to cause when message is unavailable")
    void fallsBackToCause() {
        var error = PodmanError.builder().message(" ").reason("not found").build();

        var thrown = PodmanApiException.of(PULL, 404, error, "fallback");

        assertTrue(thrown.getMessage().contains("not found"));
        assertEquals("not found", thrown.getReason());
    }

    @Test
    @DisplayName("[Success] Uses the fallback message when the error body is absent")
    void toleratesMissingBody() {
        var thrown = PodmanApiException.of(PULL, 500, null, "fallback");

        assertTrue(thrown.getMessage().contains("fallback"));
        assertNull(thrown.getReason());
    }
}
