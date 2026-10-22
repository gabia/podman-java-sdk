/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import com.gabiacloud.podman.exception.PodmanApiException;
import com.gabiacloud.podman.exception.PodmanClientException;
import com.gabiacloud.podman.model.PodmanError;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Single entry point that every Podman request passes through
 */
@AllArgsConstructor
public final class PodmanRequestExecutor {

    private static final String UNKNOWN_ERROR = "Unknown error";

    private static final int ERROR_STATUS = 400;

    private final @NonNull PodmanRequestProcessor processor;

    private final @NonNull ErrorMessageResolver errorMessageResolver;

    /**
     * Sends a request and reads the whole body. 404 is returned as is
     *
     * @return response with a status code below 400, or 404
     * @throws PodmanClientException if the exchange fails
     * @throws PodmanApiException if the status code is 400 or higher and not 404
     */
    @NonNull public PodmanResponse<byte[]> process(@NonNull HttpRequest request) {
        var response = send(request);
        if (!response.isNotFound()) {
            assertSuccess(response);
        }

        return response;
    }

    /**
     * Sends a request whose target must exist and reads the whole body
     *
     * @return response with a status code below 400
     * @throws PodmanClientException if the exchange fails
     * @throws PodmanApiException if the status code is 400 or higher, including 404
     */
    @NonNull public PodmanResponse<byte[]> processRequired(@NonNull HttpRequest request) {
        var response = send(request);
        assertSuccess(response);

        return response;
    }

    /**
     * Sends a request and receives the body as a raw stream
     *
     * @return response with a status code below 400
     * @throws PodmanClientException if the exchange fails
     * @throws PodmanApiException if the status code is 400 or higher, including 404
     */
    @NonNull public PodmanResponse<InputStream> stream(@NonNull HttpRequest request) {
        PodmanResponse<InputStream> response;
        try {
            response = processor.stream(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PodmanClientException("interrupted while streaming " + request.uri(), e);
        } catch (ConnectException e) {
            throw new PodmanClientException("cannot connect to podman " + request.uri(), e);
        } catch (IOException | RuntimeException e) {
            throw new PodmanClientException("cannot stream request " + request.uri(), e);
        }

        if (response.getStatusCode() < ERROR_STATUS) {
            return response;
        }

        byte[] body;
        try (var stream = response.getBody()) {
            body = stream.readAllBytes();
        } catch (IOException e) {
            throw new PodmanClientException("cannot read the error response of " + request.uri(), e);
        }

        var text = new String(body, StandardCharsets.UTF_8).lines().collect(Collectors.joining(" "));
        throw toException(response.getUri(), response.getStatusCode(), body, text);
    }

    @NonNull private PodmanResponse<byte[]> send(@NonNull HttpRequest request) {
        try {
            return processor.apply(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PodmanClientException("interrupted while sending request " + request.uri(), e);
        } catch (ConnectException e) {
            throw new PodmanClientException("cannot connect to podman " + request.uri(), e);
        } catch (IOException | RuntimeException e) {
            throw new PodmanClientException("cannot send request " + request.uri(), e);
        }
    }

    private void assertSuccess(@NonNull PodmanResponse<byte[]> response) {
        if (response.getStatusCode() >= ERROR_STATUS) {
            throw toException(response.getUri(), response.getStatusCode(), response.getBody(), UNKNOWN_ERROR);
        }
    }

    @NonNull private PodmanApiException toException(
            @NonNull URI uri, int statusCode, byte @NonNull [] body, @NonNull String fallbackMessage) {
        return PodmanApiException.of(uri, statusCode, resolve(body), fallbackMessage);
    }

    @Nullable private PodmanError resolve(byte @NonNull [] body) {
        if (body.length == 0) {
            return null;
        }

        try {
            return errorMessageResolver.resolve(body);
        } catch (IOException e) {
            return null;
        }
    }
}
