package com.gabiacloud.podman.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.exception.PodmanClientException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.http.HttpRequest;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * Single entry point that every Podman request passes through
 */
@AllArgsConstructor
public final class PodmanRequestExecutor {

    private final @NonNull ObjectMapper mapper;

    private final @NonNull PodmanRequestProcessor processor;

    /**
     * Sends a request and receives a JSON response
     *
     * @return response carrying the status code as is
     * @throws PodmanClientException if the exchange fails
     */
    @NonNull public PodmanResponse<JsonNode> process(@NonNull HttpRequest request) {
        try {
            return processor.apply(mapper, request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PodmanClientException("interrupted while sending request " + request.uri(), e);
        } catch (ConnectException e) {
            throw new PodmanClientException("cannot connect to podman " + request.uri(), e);
        } catch (IOException | RuntimeException e) {
            throw new PodmanClientException("cannot send request " + request.uri(), e);
        }
    }

    /**
     * Sends a request and receives the body as a raw stream
     *
     * @throws PodmanClientException if the exchange fails
     */
    @NonNull public PodmanResponse<InputStream> stream(@NonNull HttpRequest request) {
        try {
            return processor.stream(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PodmanClientException("interrupted while streaming " + request.uri(), e);
        } catch (ConnectException e) {
            throw new PodmanClientException("cannot connect to podman " + request.uri(), e);
        } catch (IOException | RuntimeException e) {
            throw new PodmanClientException("cannot stream request " + request.uri(), e);
        }
    }
}
