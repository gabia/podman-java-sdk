/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

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

    private static final int NOT_FOUND = 404;

    private final int statusCode;

    /** Request URI */
    private final @NonNull URI uri;

    private final T body;

    /** Whether the status code is 404 */
    public boolean isNotFound() {
        return statusCode == NOT_FOUND;
    }
}
