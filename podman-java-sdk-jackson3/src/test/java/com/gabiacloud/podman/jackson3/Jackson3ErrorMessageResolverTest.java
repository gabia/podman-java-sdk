/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

final class Jackson3ErrorMessageResolverTest {

    private final Jackson3ErrorMessageResolver resolver =
            new Jackson3ErrorMessageResolver(JsonMapper.builder().build());

    @Test
    @DisplayName("[Success] Reads message and cause from a libpod error body")
    void readsLibpodError() throws IOException {
        var error =
                resolver.resolve(bytes("{\"cause\":\"no such pod\",\"message\":\"pod not found\",\"response\":404}"));

        assertNotNull(error);
        assertEquals("pod not found", error.getMessage());
        assertEquals("no such pod", error.getReason());
    }

    @Test
    @DisplayName("[Failure] Throws IOException for a body that is not JSON")
    void rejectsNonJson() {
        assertThrows(IOException.class, () -> resolver.resolve(bytes("<html>502 Bad Gateway</html>")));
    }

    private static byte[] bytes(String body) {
        return body.getBytes(StandardCharsets.UTF_8);
    }
}
