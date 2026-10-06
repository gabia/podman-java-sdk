package com.gabiacloud.podman.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.property.PodmanProperty;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import org.jspecify.annotations.NonNull;

/**
 * Layer that sends requests to Podman
 */
public interface PodmanRequestProcessor {

    /** Creates a request processor that matches the host setting */
    @NonNull static PodmanRequestProcessor forHost(@NonNull PodmanProperty properties) {
        return properties.isUnixSocket()
                ? new UnixSocketPodmanRequestProcessor(properties)
                : new HttpClientPodmanRequestProcessor();
    }

    /**
     * Sends a request and reads a JSON response
     *
     * @param mapper object mapper used to convert the JSON response
     * @param request request to send
     * @return response carrying the status code as is
     * @throws IOException if the exchange fails
     * @throws InterruptedException if interrupted while waiting
     */
    @NonNull PodmanResponse<JsonNode> apply(@NonNull ObjectMapper mapper, @NonNull HttpRequest request)
            throws IOException, InterruptedException;

    /**
     * Sends a request and reads the body as a raw stream
     *
     * @throws IOException if the exchange fails
     * @throws InterruptedException if interrupted while waiting
     */
    @NonNull PodmanResponse<InputStream> stream(@NonNull HttpRequest request) throws IOException, InterruptedException;
}
