/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.exception;

import java.net.URI;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Thrown when an API request fails
 */
@Getter
public final class PodmanApiException extends PodmanClientException {

    private final @NonNull URI uri;

    private final int statusCode;

    /** {@code cause} from the libpod error body */
    private final @Nullable String reason;

    /** Creates an exception without a message */
    public PodmanApiException(@NonNull URI uri, int statusCode) {
        this(uri, statusCode, null, null, null);
    }

    /** Creates an exception with a message */
    public PodmanApiException(@NonNull URI uri, int statusCode, @NonNull String message) {
        this(uri, statusCode, message, null, null);
    }

    /** Creates an exception that includes the libpod {@code cause} */
    public PodmanApiException(@NonNull URI uri, int statusCode, @NonNull String message, @Nullable String reason) {
        this(uri, statusCode, message, reason, null);
    }

    /** Creates an exception with a cause */
    public PodmanApiException(
            @NonNull URI uri,
            int statusCode,
            @Nullable String message,
            @Nullable String reason,
            @Nullable Throwable cause) {
        super("Uri: %s, Status Code: %d, Message: %s".formatted(uri, statusCode, message), cause);
        this.uri = uri;
        this.statusCode = statusCode;
        this.reason = reason;
    }
}
