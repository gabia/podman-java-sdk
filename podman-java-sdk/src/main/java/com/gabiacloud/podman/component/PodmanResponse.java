package com.gabiacloud.podman.component;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * Response to a Podman request
 */
@Getter
@AllArgsConstructor
public final class PodmanResponse<T> {

    private final int statusCode;

    /** Request URI */
    private final @NonNull URI uri;

    private final T body;
}
