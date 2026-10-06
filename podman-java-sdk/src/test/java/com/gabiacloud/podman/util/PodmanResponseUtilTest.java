package com.gabiacloud.podman.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gabiacloud.podman.component.PodmanResponse;
import com.gabiacloud.podman.exception.PodmanApiException;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

final class PodmanResponseUtilTest {

    private static final URI PATH = URI.create("/v6.0.0/libpod/pods/gabiacloud-lb-1/json");

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    /** Builds a response with only a status code and body. */
    private PodmanResponse<JsonNode> response(int statusCode, String body) {
        return new PodmanResponse<>(statusCode, PATH, body == null ? null : parse(body));
    }

    private JsonNode parse(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException(json, e);
        }
    }

    @Nested
    @DisplayName("Status code handling")
    final class Status {

        @Test
        @DisplayName("[Success] handleDefault accepts 404 and successful responses")
        void handleDefaultPassesNotFound() {
            assertDoesNotThrow(() -> PodmanResponseUtil.handleDefault(response(404, "{\"cause\":\"no such pod\"}")));
            assertDoesNotThrow(() -> PodmanResponseUtil.handleDefault(response(204, null)));
            assertDoesNotThrow(() -> PodmanResponseUtil.handleDefault(response(304, null)));
        }

        @Test
        @DisplayName("[Failure] handleDefault throws on errors other than 404")
        void handleDefaultThrowsOnOtherFailures() {
            var thrown = assertThrows(
                    PodmanApiException.class,
                    () -> PodmanResponseUtil.handleDefault(response(409, "{\"cause\":\"pod is running\"}")));

            assertEquals(409, thrown.getStatusCode());
        }

        @Test
        @DisplayName("[Failure] error404 throws on a 404 response")
        void error404ThrowsOnNotFound() {
            var thrown = assertThrows(
                    PodmanApiException.class,
                    () -> PodmanResponseUtil.error404(
                            response(404, "{\"cause\":\"no such pod\",\"message\":\"pod not found\"}")));

            assertEquals(404, thrown.getStatusCode());
            assertEquals("no such pod", thrown.getReason());
            assertTrue(thrown.getMessage().contains("pod not found"));
        }
    }

    @Nested
    @DisplayName("Per-container failures")
    final class ContainerErrors {

        @Test
        @DisplayName("[Success] Passes when Errs is missing, empty, or contains only null values")
        void passesWhenNoErrors() {
            assertDoesNotThrow(() -> PodmanResponseUtil.assertNoContainerErrors(response(200, "{\"Id\":\"1\"}")));
            assertDoesNotThrow(() -> PodmanResponseUtil.assertNoContainerErrors(response(200, "{\"Errs\":[]}")));
            assertDoesNotThrow(() -> PodmanResponseUtil.assertNoContainerErrors(response(200, "{\"Errs\":null}")));
            assertDoesNotThrow(
                    () -> PodmanResponseUtil.assertNoContainerErrors(response(200, "{\"Errs\":[null,null]}")));
        }

        @Test
        @DisplayName("[Failure] Throws with multiple error reasons joined together")
        void joinsMessages() {
            var thrown = assertThrows(
                    PodmanApiException.class,
                    () -> PodmanResponseUtil.assertNoContainerErrors(
                            response(200, "{\"Errs\":[\"haproxy: no such image\",null,\"keepalived: denied\"]}")));

            assertTrue(thrown.getMessage().contains("haproxy: no such image; keepalived: denied"));
            assertEquals(200, thrown.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Error body parsing")
    final class ErrorBody {

        private static final URI PULL = URI.create("http://d/v6.0.0/libpod/images/pull");

        @Test
        @DisplayName("[Success] Uses message when available")
        void prefersMessage() {
            var thrown = PodmanResponseUtil.toException(
                    PULL, 404, parse("{\"message\":\"manifest unknown\",\"cause\":\"not found\"}"), "fallback");

            assertTrue(thrown.getMessage().contains("manifest unknown"));
            assertEquals("not found", thrown.getReason());
        }

        @Test
        @DisplayName("[Success] Falls back to cause when message is unavailable")
        void fallsBackToCause() {
            var thrown = PodmanResponseUtil.toException(PULL, 404, parse("{\"cause\":\"not found\"}"), "fallback");

            assertTrue(thrown.getMessage().contains("not found"));
            assertEquals("not found", thrown.getReason());
        }

        @Test
        @DisplayName("[Success] Uses the fallback message when the error body is absent")
        void toleratesMissingBody() {
            var thrown = PodmanResponseUtil.toException(PULL, 500, null, "fallback");

            assertTrue(thrown.getMessage().contains("fallback"));
            assertNull(thrown.getReason());
        }
    }
}
