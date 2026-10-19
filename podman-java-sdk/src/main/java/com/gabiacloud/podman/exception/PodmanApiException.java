/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.exception;

import com.gabiacloud.podman.model.PodmanError;
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

    /**
     * Creates an exception from a libpod error body
     *
     * @param error the error body, or {@code null} if it is absent or not in libpod format
     * @param fallbackMessage message to use when both {@code message} and {@code cause} are absent
     */
    @NonNull public static PodmanApiException of(
            @NonNull URI uri, int statusCode, @Nullable PodmanError error, @NonNull String fallbackMessage) {
        var message = error == null ? null : blankToNull(error.getMessage());
        var reason = error == null ? null : blankToNull(error.getReason());
        if (message == null) {
            message = reason == null ? fallbackMessage : reason;
        }

        return new PodmanApiException(uri, statusCode, message, reason);
    }

    @Nullable private static String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
